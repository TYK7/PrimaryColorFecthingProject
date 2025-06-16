# Brand Analyzer UI

## Description

This Angular application provides a user interface for the Brand Insights API. Users can enter a website URL, and the application will fetch and display various brand-related assets such as the brand name, logo, color palettes, fonts, and images from the specified website.

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 12.x.x.

## Prerequisites

*   **Node.js**: Version 14.x or 16.x is recommended for Angular 12. (The project was scaffolded using Node.js 18.19.1, which might show a warning but generally works for development).
*   **Angular CLI**: Version 12.x.x. If you don't have it, you can install it globally: `npm install -g @angular/cli@^12.0.0`
*   **Backend API**: A running instance of the `brand-insights-api` (the Spring Boot backend for this project). By default, it's expected to be running on `http://localhost:8080`.

## Setup and Installation

1.  **Navigate to the project directory:**
    ```bash
    cd brand-analyzer-ui
    ```

2.  **Install dependencies:**
    ```bash
    npm install
    ```
    (If you encounter issues related to Node.js version, consider using a version manager like `nvm` to switch to a compatible Node.js version like v14 or v16).

## Development Server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

```bash
ng serve
```

**Important: Backend API Proxy Configuration**

For the Angular development server (usually on port 4200) to communicate with the backend API (usually on port 8080) without running into CORS (Cross-Origin Resource Sharing) issues, you need to set up a proxy.

1.  **Create `proxy.conf.json`:**
    Create a file named `proxy.conf.json` in the root of the `brand-analyzer-ui` project with the following content:

    ```json
    {
      "/api": {
        "target": "http://localhost:8080",
        "secure": false,
        "changeOrigin": true,
        "logLevel": "debug"
      }
    }
    ```
    This configuration tells the Angular development server to forward any requests made to `/api/*` to `http://localhost:8080/api/*`.

2.  **Run `ng serve` with proxy:**
    Start the development server using the proxy configuration:

    ```bash
    ng serve --proxy-config proxy.conf.json
    ```

    Alternatively, you can add the proxy configuration to your `angular.json` file under the `serve` target's options:
    ```json
    // In angular.json, inside projects.brand-analyzer-ui.architect.serve.options
    "proxyConfig": "proxy.conf.json"
    ```
    If you add it to `angular.json`, you can just run `ng serve`.

## Code Scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build:

```bash
ng build --prod
```
(For Angular 12, `--prod` is equivalent to `--configuration production`)

## Further Help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
