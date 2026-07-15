package org.example.homework.service;

import org.example.homework.dto.response.AvailableResourceItem;
import org.example.homework.dto.response.OverloadedEmployeeItem;
import org.example.homework.dto.response.UtilizationReportItem;
import java.util.List;

public interface ReportService {
    List<UtilizationReportItem> getUtilizationReport();
    List<AvailableResourceItem> getAvailableResourcesReport();
    List<OverloadedEmployeeItem> getOverloadedEmployeesReport();
}
