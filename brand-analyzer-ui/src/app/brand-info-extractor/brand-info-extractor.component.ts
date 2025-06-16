import { Component, OnInit } from '@angular/core';
import { BrandApiService, BrandInfoResponse } from '../brand-api.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-brand-info-extractor',
  templateUrl: './brand-info-extractor.component.html',
  styleUrls: ['./brand-info-extractor.component.scss']
})
export class BrandInfoExtractorComponent implements OnInit {
  public websiteUrl: string = '';
  public brandData: BrandInfoResponse | null = null;
  public isLoading: boolean = false;
  public errorMessage: string | null = null;
  public primaryWebsiteColor: string | null = null;

  constructor(private brandApiService: BrandApiService) { }

  ngOnInit(): void {
  }

  fetchBrandData(): void {
    if (!this.websiteUrl || this.websiteUrl.trim() === '') {
      this.errorMessage = "Please enter a website URL.";
      this.brandData = null;
      this.primaryWebsiteColor = null;
      this.isLoading = false; // Ensure loading stops
      return;
    }

    console.log('Fetching data for URL:', this.websiteUrl);
    this.isLoading = true;
    this.brandData = null;
    this.errorMessage = null;
    this.primaryWebsiteColor = null;

    this.brandApiService.fetchBrandInfo(this.websiteUrl)
      .pipe(
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: (response: BrandInfoResponse) => {
          if (response.success) {
            this.brandData = response;
            this.errorMessage = null;
            console.log('API Success Response:', response);

            if (response.websiteColors && response.websiteColors.length > 0) {
              const primaryColorObj = response.websiteColors.find(c => c.label.toLowerCase() === 'primary');
              if (primaryColorObj) {
                this.primaryWebsiteColor = primaryColorObj.colorHex;
              } else {
                if (response.logoColors && response.logoColors.length > 0) {
                    this.primaryWebsiteColor = response.logoColors[0];
                } else if (response.websiteColors.length > 0) { // Check length before accessing index 0
                    this.primaryWebsiteColor = response.websiteColors[0].colorHex;
                }
              }
            } else if (response.logoColors && response.logoColors.length > 0) {
                 this.primaryWebsiteColor = response.logoColors[0];
            }

          } else {
            this.errorMessage = response.errorMessage || 'An unknown error occurred processing the data.';
            this.brandData = null; // Keep brandData null if success is false
            this.primaryWebsiteColor = null;
            console.error('API Error Response:', response);
          }
        },
        error: (errorResponse: BrandInfoResponse) => {
          console.error('API Call Failed:', errorResponse);
          this.errorMessage = errorResponse.errorMessage || 'Failed to fetch data. Please check the URL or network connection.';
          this.brandData = null; // Keep brandData null on error
          this.primaryWebsiteColor = null;
        }
      });
  }

  // Getter to check if there are any meaningful results to display
  public get hasMeaningfulResults(): boolean {
    if (!this.brandData || !this.brandData.success) {
      return false;
    }
    // Check if any of the key data points are present
    // Also consider brandName if it's not a generic "not found" message
    const meaningfulBrandName = this.brandData.brandName &&
                                this.brandData.brandName.toLowerCase() !== 'brand name not found' &&
                                !this.brandData.brandName.toLowerCase().startsWith('error:');

    return !!(
      meaningfulBrandName ||
      (this.brandData.logoUrl && this.brandData.logoUrl.trim() !== '') ||
      (this.brandData.logoColors && this.brandData.logoColors.length > 0) ||
      (this.brandData.websiteColors && this.brandData.websiteColors.length > 0) ||
      (this.brandData.fonts && this.brandData.fonts.length > 0) ||
      (this.brandData.imageUrls && this.brandData.imageUrls.length > 0)
    );
  }
}
