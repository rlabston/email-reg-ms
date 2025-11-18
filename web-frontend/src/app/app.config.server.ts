import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering, withRoutes, withAppShell } from '@angular/ssr';
import { appConfig } from './app.config';
import { serverRoutes } from './app.routes.server';
import { AppShell } from './app-shell.component';

const serverConfig: ApplicationConfig = {
  providers: [
    // Provide server rendering with routes and the AppShell so the server
    // renders the same host structure (<router-outlet>) that the client
    // bootstraps. This keeps server output consistent with the client
    // template and avoids hydration mismatches (NG0500).
    provideServerRendering(withRoutes(serverRoutes), withAppShell(AppShell))
  ]
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
