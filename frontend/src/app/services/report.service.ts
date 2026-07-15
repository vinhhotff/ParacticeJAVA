import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UtilizationReportItem, AvailableResourceItem, OverloadedEmployeeItem } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private baseUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  getUtilizationReport(): Observable<UtilizationReportItem[]> {
    return this.http.get<UtilizationReportItem[]>(`${this.baseUrl}/utilization`);
  }

  getAvailableResourcesReport(): Observable<AvailableResourceItem[]> {
    return this.http.get<AvailableResourceItem[]>(`${this.baseUrl}/available-resources`);
  }

  getOverloadedEmployeesReport(): Observable<OverloadedEmployeeItem[]> {
    return this.http.get<OverloadedEmployeeItem[]>(`${this.baseUrl}/overloaded`);
  }
}
