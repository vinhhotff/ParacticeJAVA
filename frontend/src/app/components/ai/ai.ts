import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiService } from '../../services/ai.service';
import { ResourceRecommendation, RiskReport } from '../../models';

@Component({
  selector: 'app-ai',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai.html',
  styleUrl: './ai.css'
})
export class Ai implements OnInit {
  protected readonly searchRole = signal('');
  protected readonly minAvailable = signal(30);
  protected readonly customPrompt = signal('');
  protected readonly loadingRecs = signal(false);
  protected readonly loadingRisks = signal(true);
  
  protected readonly recommendations = signal<ResourceRecommendation[]>([]);
  protected readonly riskReport = signal<RiskReport | null>(null);
  protected readonly searchTriggered = signal(false);

  // Workload Summary Pagination
  protected readonly workloadPage = signal(1);
  protected readonly workloadPageSize = 5;
  protected readonly paginatedWorkloads = computed(() => {
    const report = this.riskReport();
    if (!report || !report.workloadSummary) return [];
    
    const workloads = report.workloadSummary;
    const total = workloads.length;
    const page = Math.min(this.workloadPage(), Math.ceil(total / this.workloadPageSize) || 1);
    const startIndex = (page - 1) * this.workloadPageSize;
    return workloads.slice(startIndex, startIndex + this.workloadPageSize);
  });
  protected readonly workloadTotalPages = computed(() => {
    const report = this.riskReport();
    if (!report || !report.workloadSummary) return 1;
    return Math.max(1, Math.ceil(report.workloadSummary.length / this.workloadPageSize));
  });

  protected goToWorkloadPage(page: number): void {
    if (page >= 1 && page <= this.workloadTotalPages()) {
      this.workloadPage.set(page);
    }
  }

  constructor(private aiService: AiService) {}

  ngOnInit(): void {
    this.loadProjectRisks();
  }

  loadProjectRisks(): void {
    this.loadingRisks.set(true);
    this.aiService.getProjectRisks(this.customPrompt()).subscribe({
      next: (data) => {
        this.riskReport.set(data);
        this.workloadPage.set(1);
        this.loadingRisks.set(false);
      },
      error: (err) => {
        console.error('Error loading project risks', err);
        this.loadingRisks.set(false);
      }
    });
  }

  findRecommendations(): void {
    if (!this.searchRole().trim()) return;

    this.loadingRecs.set(true);
    this.searchTriggered.set(true);

    this.aiService.getRecommendations(this.searchRole(), this.minAvailable()).subscribe({
      next: (data) => {
        this.recommendations.set(data);
        this.loadingRecs.set(false);
      },
      error: (err) => {
        console.error('Error loading recommendations', err);
        this.loadingRecs.set(false);
      }
    });
  }
}
