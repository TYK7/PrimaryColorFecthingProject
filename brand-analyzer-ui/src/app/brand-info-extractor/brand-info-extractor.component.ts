import { Component, OnInit } from '@angular/core';
import { BrandApiService, BrandInfoResponse } from '../brand-api.service'; // Import service and interface
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-brand-info-extractor',
  templateUrl: './brand-info-extractor.component.html',
  styleUrls: ['./brand-info-extractor.component.scss']
})
export class BrandInfoExtractorComponent implements OnInit {
  public websiteUrl: string = '';
  public brandData: BrandInfoResponse | null = null; // Typed property
  public isLoading: boolean = false;
  public errorMessage: string | null = null;

  constructor(private brandApiService: BrandApiService) { } // Inject service

  ngOnInit(): void {
  }

  fetchBrandData(): void {
    if (!this.websiteUrl || this.websiteUrl.trim() === '') {
      this.errorMessage = "Please enter a website URL.";
      this.brandData = null;
      this.isLoading = false; // Stop loading if URL is empty
      return;
    }

    console.log('Fetching data for URL:', this.websiteUrl);
    this.isLoading = true;
    this.brandData = null;
    this.errorMessage = null;

    this.brandApiService.fetchBrandInfo(this.websiteUrl)
      .pipe(
        finalize(() => {
          this.isLoading = false; // Ensure loading is stopped in all cases
        })
      )
      .subscribe({
        next: (response: BrandInfoResponse) => {
          if (response.success) {
            this.brandData = response;
            this.errorMessage = null;
            // Log the full response for debugging during development
            console.log('API Success Response:', response);
          } else {
            this.errorMessage = response.errorMessage || 'An unknown error occurred processing the data.';
            this.brandData = null;
            console.error('API Error Response (success=false):', response);
          }
        },
        error: (errorResponse: BrandInfoResponse) => { // Error from service's handleError or network
          console.error('API Call Failed (Observable error):', errorResponse);
          // The errorResponse should be the object crafted by BrandApiService.handleError
          this.errorMessage = errorResponse.errorMessage || 'Failed to fetch data. Please check the URL or network connection.';
          this.brandData = null;
        }
      });
  }
}
