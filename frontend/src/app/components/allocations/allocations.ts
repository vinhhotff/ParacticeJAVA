import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AllocationService } from '../../services/allocation.service';
import { EmployeeService } from '../../services/employee.service';
import { ProjectService } from '../../services/project.service';
import { Allocation, Employee, Project } from '../../models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-allocations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './allocations.html',
  styleUrl: './allocations.css'
})
export class Allocations implements OnInit {
  protected readonly allocations = signal<Allocation[]>([]);
  protected readonly employees = signal<Employee[]>([]);
  protected readonly projects = signal<Project[]>([]);
  protected readonly loading = signal(true);
  protected readonly showCreateModal = signal(false);

  // Pagination
  protected readonly currentPage = signal(1);
  protected readonly pageSize = 5;
  protected readonly paginatedAllocations = computed(() => {
    const total = this.allocations().length;
    const page = Math.min(this.currentPage(), Math.ceil(total / this.pageSize) || 1);
    const startIndex = (page - 1) * this.pageSize;
    return this.allocations().slice(startIndex, startIndex + this.pageSize);
  });
  protected readonly totalPages = computed(() => {
    return Math.max(1, Math.ceil(this.allocations().length / this.pageSize));
  });

  protected goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  protected readonly newAllocation = signal<any>({
    employeeId: null,
    projectId: null,
    allocationPercent: null,
    roleInProject: '',
    startDate: '',
    endDate: ''
  });

  protected readonly errorMessage = signal<string | null>(null);

  constructor(
    private allocationService: AllocationService,
    private employeeService: EmployeeService,
    private projectService: ProjectService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    forkJoin({
      allocations: this.allocationService.getAll(),
      employees: this.employeeService.getAll(),
      projects: this.projectService.getAll()
    }).subscribe({
      next: (data) => {
        this.allocations.set(data.allocations);
        this.employees.set(data.employees);
        this.projects.set(data.projects);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading allocations data', err);
        this.loading.set(false);
      }
    });
  }

  openCreateModal(): void {
    this.newAllocation.set({
      employeeId: this.employees().length > 0 ? this.employees()[0].id : null,
      projectId: this.projects().length > 0 ? this.projects()[0].id : null,
      allocationPercent: 50,
      roleInProject: '',
      startDate: new Date().toISOString().substring(0, 10),
      endDate: ''
    });
    this.errorMessage.set(null);
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  createAllocation(): void {
    this.errorMessage.set(null);
    const alloc = { ...this.newAllocation() };
    
    // Parse types
    alloc.employeeId = Number(alloc.employeeId);
    alloc.projectId = Number(alloc.projectId);
    alloc.allocationPercent = Number(alloc.allocationPercent);
    
    if (!alloc.endDate) {
      delete alloc.endDate;
    }

    this.allocationService.create(alloc).subscribe({
      next: (created) => {
        // Reload allocations to fetch populated employee/project names from server mapping
        this.allocationService.getAll().subscribe(data => this.allocations.set(data));
        this.closeCreateModal();
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to create allocation. Check if capacity bounds (100%) or status limits are violated.');
      }
    });
  }

  deleteAllocation(id: number): void {
    if (confirm('Are you sure you want to end this project allocation?')) {
      this.allocationService.delete(id).subscribe({
        next: () => {
          this.allocations.update(arr => arr.filter(a => a.id !== id));
        },
        error: (err) => {
          console.error('Error deleting allocation', err);
        }
      });
    }
  }
}
