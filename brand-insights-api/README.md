# Brand Insights API

## Description

This Spring Boot application provides a REST API to extract brand-related information from a given website URL. It can identify the brand name, logo, dominant colors in the logo, top colors used on the website, fonts, and images present on the homepage.

## Prerequisites

*   Java Development Kit (JDK) 1.8 or higher
*   Apache Maven 3.2+

## How to Build

To build the project and create an executable JAR file, navigate to the project's root directory (`brand-insights-api`) and run the following Maven command:

```bash
mvn clean package
```

This will compile the code, run tests (if any were not skipped), and package the application into `target/brand-insights-api-0.0.1-SNAPSHOT.jar`.

## How to Run

Once the project is built, you can run the application using:

```bash
java -jar target/brand-insights-api-0.0.1-SNAPSHOT.jar
```

The API server will start, typically on port 8080 (unless configured otherwise).

## API Endpoint

### Extract Brand Information

*   **URL:** `/api/extract-brand-info`
*   **Method:** `POST`
*   **Content-Type:** `application/json`

#### Request Body

```json
{
  "url": "https://www.example.com"
}
```

*   `url` (string, required): The full URL of the website to analyze.

#### Successful Response (200 OK)

The API returns a JSON object with the extracted brand information.

```json
{
  "success": true,
  "errorMessage": null,
  "brandName": "Example Brand Name",
  "logoUrl": "https://example.com/path/to/logo.png",
  "logoColors": ["#RRGGBB", "#RRGGBB", ...],
  "websiteColors": [
    { "colorHex": "#RRGGBB", "label": "Primary" },
    { "colorHex": "#RRGGBB", "label": "Secondary" },
    { "colorHex": "#RRGGBB", "label": "Tertiary" },
    ...
  ],
  "fonts": ["Font Name 1", "Font Name 2", ...],
  "imageUrls": [
    "https://example.com/path/to/image1.jpg",
    "https://example.com/path/to/image2.png",
    ...
  ]
}
```

*   `success` (boolean): `true` if the operation was successful.
*   `errorMessage` (string|null): `null` on success, or an error message if `success` is `false`.
*   `brandName` (string|null): Extracted brand name.
*   `logoUrl` (string|null): Absolute URL to the brand logo.
*   `logoColors` (array of strings|null): List of HEX color codes from the logo.
*   `websiteColors` (array of objects|null): List of top website colors, each with `colorHex` and `label`.
*   `fonts` (array of strings|null): List of unique font names found.
*   `imageUrls` (array of strings|null): List of absolute URLs of images on the homepage.

#### Error Responses

If the request is invalid or an error occurs during processing, the API will return an appropriate HTTP status code (e.g., 400, 422, 500) and a JSON body like:

```json
{
  "success": false,
  "errorMessage": "Specific error message detailing what went wrong.",
  "brandName": null,
  "logoUrl": null,
  "logoColors": [],
  "websiteColors": [],
  "fonts": [],
  "imageUrls": []
}
```

Common error messages include:
*   "Request body or URL must not be empty." (HTTP 400)
*   "Invalid URL format: ..." (HTTP 400)
*   "Could not reach host: ..." (HTTP 400)
*   "Connection timed out..." (HTTP 400)
*   "Could not fetch content from the URL: ..." (HTTP 422)
*   "An unexpected error occurred: ..." (HTTP 422 or 500)
