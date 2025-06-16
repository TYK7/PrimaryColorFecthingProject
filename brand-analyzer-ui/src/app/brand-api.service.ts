import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

// Interfaces matching backend DTOs
export interface BrandInfoRequest {
  url: string;
}

export interface WebsiteColor {
  colorHex: string;
  label: string;
}

export interface BrandInfoResponse {
  success: boolean;
  errorMessage?: string;
  brandName?: string;
  logoUrl?: string;
  logoColors?: string[];
  websiteColors?: WebsiteColor[];
  fonts?: string[];
  imageUrls?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class BrandApiService {
  private apiUrl = '/api/extract-brand-info'; // Relative path to backend API

  constructor(private http: HttpClient) { }

  fetchBrandInfo(url: string): Observable<BrandInfoResponse> {
    const requestPayload: BrandInfoRequest = { url };
    return this.http.post<BrandInfoResponse>(this.apiUrl, requestPayload)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse): Observable<BrandInfoResponse> {
    let userMessage = 'Something bad happened; please try again later.';

    // Default error response structure
    const errorResponse: BrandInfoResponse = {
        success: false,
        errorMessage: userMessage,
        // Initialize other fields to ensure a consistent object shape
        brandName: undefined,
        logoUrl: undefined,
        logoColors: [],
        websiteColors: [],
        fonts: [],
        imageUrls: []
    };

    if (error.status === 0 || error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error?.message || error.message);
      userMessage = `Client-side/network error: ${error.error?.message || error.message}`;
      if (error.status === 0) {
        userMessage = 'Cannot connect to the server. Please check your network or if the server is running.';
      }
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong.
      console.error(
        `Backend returned code ${error.status}, ` +
        `body was: ${JSON.stringify(error.error)}`);

      // Try to use the error message from the backend's BrandInfoResponse
      if (error.error && typeof error.error.errorMessage === 'string' && error.error.errorMessage.trim() !== '') {
        userMessage = error.error.errorMessage;
      } else {
        userMessage = `Server error: ${error.status} - ${error.statusText || 'Unknown server error'}`;
      }
    }

    errorResponse.errorMessage = userMessage;
    return throwError(() => errorResponse);
  }
}
