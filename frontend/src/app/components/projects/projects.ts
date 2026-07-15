import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProjectService } from '../../services/project.service';
import { Project } from '../../models';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './projects.html',
  styleUrl: './projects.css'
})
export class Projects implements OnInit {
  protected readonly projects = signal<Project[]>([]);
  protected readonly loading = signal(true);
  protected readonly showCreateModal = signal(false);

  protected readonly newProject = signal<Project>({
    projectCode: '',
    projectName: '',
    customer: '',
    status: 'PLANNING',
    startDate: ''
  });

  protected readonly errorMessage = signal<string | null>(null);

  constructor(private projectService: ProjectService) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.projectService.getAll().subscribe({
      next: (data) => {
        this.projects.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading projects', err);
        this.loading.set(false);
      }
    });
  }

  openCreateModal(): void {
    this.newProject.set({
      projectCode: '',
      projectName: '',
      customer: '',
      status: 'PLANNING',
      startDate: new Date().toISOString().substring(0, 10),
      endDate: ''
    });
    this.errorMessage.set(null);
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  createProject(): void {
    this.errorMessage.set(null);
    const proj = { ...this.newProject() };
    if (!proj.endDate) {
      delete proj.endDate;
    }
    this.projectService.create(proj).subscribe({
      next: (created) => {
        this.projects.update(arr => [...arr, created]);
        this.closeCreateModal();
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to create project. Please verify inputs.');
      }
    });
  }
}
