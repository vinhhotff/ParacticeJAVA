import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiService } from '../../services/ai.service';
import { ResourceRecommendation, ProjectRisk } from '../../models';

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
  protected readonly loadingRecs = signal(false);
  protected readonly loadingRisks = signal(true);
  
  protected readonly recommendations = signal<ResourceRecommendation[]>([]);
  protected readonly projectRisks = signal<ProjectRisk[]>([]);
  protected readonly searchTriggered = signal(false);

  constructor(private aiService: AiService) {}

  ngOnInit(): void {
    this.loadProjectRisks();
  }

  loadProjectRisks(): void {
    this.aiService.getProjectRisks().subscribe({
      next: (data) => {
        this.projectRisks.set(data);
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
