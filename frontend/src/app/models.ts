export interface Employee {
  employeeId?: number;
  employeeCode: String;
  fullName: String;
  email: String;
  role: String;
  department: String;
}

export interface EmployeeWorkload {
  employeeId: number;
  employeeCode: string;
  fullName: string;
  totalAllocation: number;
}

export interface Project {
  projectId?: number;
  projectCode: String;
  projectName: String;
  customer: String;
  status: String;
  startDate: String;
  endDate?: String;
}

export interface Allocation {
  allocationId?: number;
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
  fullName: string;
  role: string;
  availablePercent: number;
}

export interface ProjectRisk {
  projectId: number;
  projectCode: string;
  projectName: string;
  riskScore: number;
  riskLevel: string;
  riskMessage: string;
}
