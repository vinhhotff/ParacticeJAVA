import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeeService } from '../../services/employee.service';
import { Employee, EmployeeWorkload } from '../../models';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employees.html',
  styleUrl: './employees.css'
})
export class Employees implements OnInit {
  protected readonly employees = signal<Employee[]>([]);
  protected readonly loading = signal(true);
  protected readonly showCreateModal = signal(false);
  protected readonly selectedWorkload = signal<EmployeeWorkload | null>(null);

  protected readonly newEmployee = signal<Employee>({
    employeeCode: '',
    fullName: '',
    email: '',
    role: '',
    department: ''
  });

  protected readonly errorMessage = signal<string | null>(null);

  constructor(private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.employeeService.getAll().subscribe({
      next: (data) => {
        this.employees.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading employees', err);
        this.loading.set(false);
      }
    });
  }

  openCreateModal(): void {
    this.newEmployee.set({
      employeeCode: '',
      fullName: '',
      email: '',
      role: '',
      department: ''
    });
    this.errorMessage.set(null);
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  createEmployee(): void {
    this.errorMessage.set(null);
    this.employeeService.create(this.newEmployee()).subscribe({
      next: (created) => {
        this.employees.update(arr => [...arr, created]);
        this.closeCreateModal();
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to create employee. Double check input parameters.');
      }
    });
  }

  viewWorkload(empId: number): void {
    this.employeeService.getWorkload(empId).subscribe({
      next: (workload) => {
        this.selectedWorkload.set(workload);
      },
      error: (err) => {
        console.error('Error fetching workload', err);
      }
    });
  }

  closeWorkload(): void {
    this.selectedWorkload.set(null);
  }
}
