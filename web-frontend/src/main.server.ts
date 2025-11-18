import { BootstrapContext, bootstrapApplication } from '@angular/platform-browser';
import { AppShell } from './app/app-shell.component';
import { config } from './app/app.config.server';

// For server rendering we bootstrap the AppShell (which contains <router-outlet/>)
// so that the server renders the same host structure the client expects, and
// the route content is projected into the router-outlet. This keeps server
// output consistent with client templates and prevents hydration mismatches.
const bootstrap = (context: BootstrapContext) =>
    bootstrapApplication(AppShell, config, context);

export default bootstrap;
