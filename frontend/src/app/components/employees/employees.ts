import { Component, OnInit, signal, computed } from '@angular/core';
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
  protected readonly isEditMode = signal(false);
  protected readonly selectedEmployeeId = signal<number | null>(null);
  protected readonly selectedWorkload = signal<EmployeeWorkload | null>(null);

  // Skill Management & Search
  protected readonly skillsInput = signal<string>('');
  protected readonly searchSkill = signal<string>('');
  protected readonly searchSkillResults = signal<Array<{ employeeName: string; available: number }>>([]);
  protected readonly searchTriggered = signal(false);

  // Pagination
  protected readonly currentPage = signal(1);
  protected readonly pageSize = 5;
  protected readonly paginatedEmployees = computed(() => {
    const total = this.employees().length;
    const page = Math.min(this.currentPage(), Math.ceil(total / this.pageSize) || 1);
    const startIndex = (page - 1) * this.pageSize;
    return this.employees().slice(startIndex, startIndex + this.pageSize);
  });
  protected readonly totalPages = computed(() => {
    return Math.max(1, Math.ceil(this.employees().length / this.pageSize));
  });

  protected readonly newEmployee = signal<Employee>({
    employeeCode: '',
    fullName: '',
    email: '',
    role: '',
    department: ''
  });

  protected readonly errorMessage = signal<string | null>(null);

  constructor(private employeeService: EmployeeService) {}

  protected goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

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
    this.isEditMode.set(false);
    this.selectedEmployeeId.set(null);
    this.newEmployee.set({
      employeeCode: '',
      fullName: '',
      email: '',
      role: '',
      department: ''
    });
    this.skillsInput.set('');
    this.errorMessage.set(null);
    this.showCreateModal.set(true);
  }

  openEditModal(emp: Employee): void {
    this.isEditMode.set(true);
    this.selectedEmployeeId.set(emp.id || null);
    this.newEmployee.set({ ...emp });
    this.skillsInput.set(emp.skills ? emp.skills.join(', ') : '');
    this.errorMessage.set(null);
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  saveEmployee(): void {
    if (this.isEditMode()) {
      this.updateEmployee();
    } else {
      this.createEmployee();
    }
  }

  createEmployee(): void {
    this.errorMessage.set(null);
    this.employeeService.create(this.newEmployee()).subscribe({
      next: (created) => {
        const empId = created.id!;
        const skills = this.skillsInput().split(',')
          .map(s => s.trim())
          .filter(s => s.length > 0);

        if (skills.length > 0) {
          this.employeeService.addSkills(empId, skills).subscribe({
            next: () => {
              created.skills = skills;
              this.employees.update(arr => [...arr, created]);
              this.closeCreateModal();
            },
            error: (err) => {
              console.error('Failed to add skills', err);
              created.skills = [];
              this.employees.update(arr => [...arr, created]);
              this.closeCreateModal();
            }
          });
        } else {
          created.skills = [];
          this.employees.update(arr => [...arr, created]);
          this.closeCreateModal();
        }
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to create employee. Double check input parameters.');
      }
    });
  }

  updateEmployee(): void {
    const id = this.selectedEmployeeId();
    if (!id) return;
    this.errorMessage.set(null);
    this.employeeService.update(id, this.newEmployee()).subscribe({
      next: (updated) => {
        const skills = this.skillsInput().split(',')
          .map(s => s.trim())
          .filter(s => s.length > 0);

        this.employeeService.addSkills(id, skills).subscribe({
          next: () => {
            updated.skills = skills;
            this.employees.update(arr => arr.map(e => e.id === id ? updated : e));
            this.closeCreateModal();
          },
          error: (err) => {
            console.error('Failed to add skills', err);
            updated.skills = skills;
            this.employees.update(arr => arr.map(e => e.id === id ? updated : e));
            this.closeCreateModal();
          }
        });
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to update employee.');
      }
    });
  }

  deleteEmployee(id: number): void {
    if (confirm('Are you sure you want to delete this employee? This will also cascade delete all their allocations.')) {
      this.employeeService.delete(id).subscribe({
        next: () => {
          this.employees.update(arr => arr.filter(e => e.id !== id));
        },
        error: (err) => {
          console.error('Error deleting employee', err);
        }
      });
    }
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

  triggerSearchBySkill(): void {
    const query = this.searchSkill().trim();
    if (!query) {
      this.searchSkillResults.set([]);
      this.searchTriggered.set(false);
      return;
    }
    this.employeeService.searchBySkill(query).subscribe({
      next: (data) => {
        this.searchSkillResults.set(data);
        this.searchTriggered.set(true);
      },
      error: (err) => {
        console.error('Error searching by skill', err);
        this.searchSkillResults.set([]);
        this.searchTriggered.set(true);
      }
    });
  }
}
