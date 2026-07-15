import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeeService } from '../../services/employee.service';
import { ProjectService } from '../../services/project.service';
import { AllocationService } from '../../services/allocation.service';
import { ReportService } from '../../services/report.service';
import { UtilizationReportItem, AvailableResourceItem, OverloadedEmployeeItem } from '../../models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  protected readonly totalEmployees = signal(0);
  protected readonly totalProjects = signal(0);
  protected readonly totalAllocations = signal(0);
  protected readonly utilizationReport = signal<UtilizationReportItem[]>([]);
  protected readonly availableReport = signal<AvailableResourceItem[]>([]);
  protected readonly overloadedReport = signal<OverloadedEmployeeItem[]>([]);
  protected readonly loading = signal(true);

  constructor(
    private employeeService: EmployeeService,
    private projectService: ProjectService,
    private allocationService: AllocationService,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    forkJoin({
      employees: this.employeeService.getAll(),
      projects: this.projectService.getAll(),
      allocations: this.allocationService.getAll(),
      utilization: this.reportService.getUtilizationReport(),
      available: this.reportService.getAvailableResourcesReport(),
      overloaded: this.reportService.getOverloadedEmployeesReport()
    }).subscribe({
      next: (data) => {
        this.totalEmployees.set(data.employees.length);
        this.totalProjects.set(data.projects.length);
        this.totalAllocations.set(data.allocations.length);
        this.utilizationReport.set(data.utilization);
        this.availableReport.set(data.available);
        this.overloadedReport.set(data.overloaded);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading dashboard data', err);
        this.loading.set(false);
      }
    });
  }
}
