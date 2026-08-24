import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';

import App from './App';
import { AppProviders } from './app/providers/AppProviders';
import './index.css';

const rootElement = document.getElementById('root');

if (!rootElement) {
    throw new Error(
        'React uygulamasının bağlanacağı #root elementi bulunamadı.',
    );
}

createRoot(rootElement).render(
    <StrictMode>
        <AppProviders>
            <App />
        </AppProviders>
    </StrictMode>,
);