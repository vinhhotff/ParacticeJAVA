package com.example.bookstore.service;

import com.example.bookstore.dto.request.AuthenticationRequest;
import com.example.bookstore.dto.request.IntrospectRequest;
import com.example.bookstore.dto.request.LogoutRequest;
import com.example.bookstore.dto.request.RefreshRequest;
import com.example.bookstore.dto.response.AuthenticationResponse;
import com.example.bookstore.dto.response.IntrospectResponse;
import com.example.bookstore.exception.AppException;
import com.example.bookstore.exception.ErrorCode;
import com.example.bookstore.model.InvalidatedToken;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.InvalidatedTokenRepository;
import com.example.bookstore.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

import static java.time.Instant.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvalidatedTokenRepository tokenRepository;

    @Value("${jwt.signerKey}") // Lấy key từ file cấu hình
    protected String SIGNER_KEY;

    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        // 1. Lấy token từ request của Frontend gửi lên
        var token = request.getToken();

        // 2. Tạo bộ xác thực bằng chìa khóa bí mật (SIGNER_KEY) của mình
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        // 3. Giải mã chuỗi token loằng ngoằng thành đối tượng JWT để dễ soi
        SignedJWT signedJWT = SignedJWT.parse(token);

        // 4. Kiểm tra xem chữ ký của token có khớp với chìa khóa bí mật của mình không
        // (verified)
        boolean verified = signedJWT.verify(verifier);

        // 5. Lấy ngày hết hạn của token ra để đối chiếu
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // 6. Chốt hạ: Token hợp lệ khi chữ ký chuẩn VÀ vẫn còn hạn (sau thời điểm hiện
        // tại)
        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(user);
        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512); // Thuật toán ký

        //Payload
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()) // Chủ nhân token
                .issuer("bookstore.com") // Nguồn phát hành
                .issueTime(new Date()) // Thời điểm tạo
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli() // Hết hạn sau 1 giờ
                ))
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            // Ký tên bằng SECRET_KEY
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Không thể tạo token", e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                // 1. Tự tay dán nhãn ROLE_ cho các Vai trò
                stringJoiner.add("ROLE_" + role.getName());

                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        // 2. Không dán nhãn gì thêm cho các Quyền hạn nhỏ
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString();
    }


    private SignedJWT verifyToken(String token) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean verified = signedJWT.verify(verifier);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if(!(expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            // 1. Soi token
            var signToken = verifyToken(request.getToken());

            // 2. Lấy ID và hạn sử dụng của Token
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            // 3. Khởi tạo Sọt rác và ném nó vào
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            // (LƯU Ý CỦA SENIOR: Đừng quên khai báo private final InvalidatedTokenRepository ở đầu file Service nhé)
            tokenRepository.save(invalidatedToken);

        } catch (AppException exception) {
            // Nếu Token đã hết hạn rồi thì thôi (khỏi cho vào sọt rác vì đằng nào cũng vứt rồi)
            System.out.println("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws JOSEException,ParseException {
            var signedJWT = verifyToken(request.getToken());

            var jit = signedJWT.getJWTClaimsSet().getJWTID();
            var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            var userName = signedJWT.getJWTClaimsSet().getSubject();
            var user = userRepository.findByUsername(userName)
                    .orElseThrow(()->  new AppException(ErrorCode.USER_NOT_EXISTED));

            var token = generateToken(user);

            return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }
}
