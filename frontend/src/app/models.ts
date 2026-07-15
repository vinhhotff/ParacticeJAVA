export interface Employee {
  id?: number;
  employeeCode: string;
  fullName: string;
  email: string;
  role: string;
  department: string;
}

export interface EmployeeWorkload {
  employeeId: number;
  employeeName: string;
  totalAllocation: number;
  available: number;
}

export interface Project {
  id?: number;
  projectCode: string;
  projectName: string;
  customer: string;
  status: string;
  startDate: string;
  endDate?: string;
}

export interface Allocation {
  id?: number;
  employeeId: number;
  employeeCode?: string;
  employeeName?: string;
  projectId: number;
  projectCode?: string;
  projectName?: string;
  allocationPercent: number;
  roleInProject: string;
  startDate: string;
  endDate?: string;
}

export interface UtilizationReportItem {
  employeeId: number;
  employeeCode: string;
  fullName: string;
  totalAllocation: number;
}

export interface AvailableResourceItem {
  employeeId: number;
  employeeCode: string;
  fullName: string;
  availablePercent: number;
}

export interface OverloadedEmployeeItem {
  employeeId: number;
  employeeCode: string;
  fullName: string;
  totalAllocation: number;
}

export interface ResourceRecommendation {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  role: string;
  available: number;
  matchScore: number;
}

export interface Risk {
  type: string;
  severity: string;
  message: string;
}

export interface RiskReport {
  risks: Risk[];
  summary: string;
  workloadSummary: EmployeeWorkload[];
}
