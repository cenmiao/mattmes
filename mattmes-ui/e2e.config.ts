import type { E2EConfig } from 'e2e';
import { web } from '@e2e-dev/web';

const adminPassword = process.env.E2E_ADMIN_PASSWORD ?? 'Admin@123';

export default {
  targets: [{
    engine: web(),
    app: {
      url: process.env.APP_URL ?? 'http://localhost:3000',
      // Start the dev server yourself (npm run dev); it opens a browser on boot.
      // To let the runner own it, set:
      // command: { executable: 'npm', args: ['run', 'dev'], log: '.e2e/logs/app.log' },
    },
  }],
  tests: ['tests/**/*.e2e.ts', 'e2e-test/**/*.e2e.ts'],
  credentials: {
    admin: {
      username: process.env.E2E_ADMIN_USERNAME ?? 'admin',
      password: adminPassword,
    },
  },
} satisfies E2EConfig;
