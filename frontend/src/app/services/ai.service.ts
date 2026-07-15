import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ResourceRecommendation, RiskReport } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private baseUrl = 'http://localhost:8080/api/ai';

  constructor(private http: HttpClient) {}

  getRecommendations(role: string, minAvailable: number): Observable<ResourceRecommendation[]> {
    const params = new HttpParams()
      .set('role', role)
      .set('minAvailable', minAvailable.toString());
    return this.http.get<ResourceRecommendation[]>(`${this.baseUrl}/recommend-resources`, { params });
  }

  getProjectRisks(customPrompt?: string): Observable<RiskReport> {
    let params = new HttpParams();
    if (customPrompt) {
      params = params.set('customPrompt', customPrompt);
    }
    return this.http.get<RiskReport>(`${this.baseUrl}/detect-risks`, { params });
  }
}
