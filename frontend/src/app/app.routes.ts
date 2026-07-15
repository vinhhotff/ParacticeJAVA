import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./components/dashboard/dashboard').then(m => m.Dashboard)
  },
  {
    path: 'employees',
    loadComponent: () => import('./components/employees/employees').then(m => m.Employees)
  },
  {
    path: 'projects',
    loadComponent: () => import('./components/projects/projects').then(m => m.Projects)
  },
  {
    path: 'allocations',
    loadComponent: () => import('./components/allocations/allocations').then(m => m.Allocations)
  },
  {
    path: 'ai',
    loadComponent: () => import('./components/ai/ai').then(m => m.Ai)
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
