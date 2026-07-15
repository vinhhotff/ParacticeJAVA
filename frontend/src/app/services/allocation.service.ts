import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Allocation } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AllocationService {
  private baseUrl = 'http://localhost:8080/api/allocations';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Allocation[]> {
    return this.http.get<Allocation[]>(this.baseUrl);
  }

  getById(id: number): Observable<Allocation> {
    return this.http.get<Allocation>(`${this.baseUrl}/${id}`);
  }

  create(allocation: Allocation): Observable<Allocation> {
    return this.http.post<Allocation>(this.baseUrl, allocation);
  }

  update(id: number, allocation: Allocation): Observable<Allocation> {
    return this.http.put<Allocation>(`${this.baseUrl}/${id}`, allocation);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
