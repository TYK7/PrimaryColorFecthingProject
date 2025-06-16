import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http'; // <-- Import HttpClientModule

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrandInfoExtractorComponent } from './brand-info-extractor/brand-info-extractor.component';

@NgModule({
  declarations: [
    AppComponent,
    BrandInfoExtractorComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule // <-- Add HttpClientModule here
  ],
  providers: [], // BrandApiService is providedIn: 'root'
  bootstrap: [AppComponent]
})
export class AppModule { }
