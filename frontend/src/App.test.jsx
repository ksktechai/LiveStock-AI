import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import App from './App';
import * as React from 'react';

// Mock EventSource
const mockEventSource = {
    onmessage: null,
    onerror: null,
    close: vi.fn(),
};

class MockEventSource {
    constructor(url) {
        this.url = url;
        this.onmessage = null;
        this.onerror = null;
        this.close = vi.fn();

        // Link the instance to our control object so tests can trigger events
        mockEventSource.onmessage = (e) => this.onmessage && this.onmessage(e);
        mockEventSource.onopen = (e) => this.onopen && this.onopen(e);
        mockEventSource.onerror = (e) => this.onerror && this.onerror(e);
        // Overwrite the control object's close so we can spy on it
        mockEventSource.close = this.close;
    }
}

global.EventSource = vi.fn(function (url) {
    return new MockEventSource(url);
});

// Mock fetch
global.fetch = vi.fn();

describe('App Component', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mockEventSource.onmessage = null;
        mockEventSource.onerror = null;
        mockEventSource.close.mockClear();
    });

    it('renders correctly', () => {
        render(<App />);

        expect(screen.getByText('LiveStock AI')).toBeInTheDocument();
        expect(screen.getByText(/streaming SSE updates from the backend/i)).toBeInTheDocument();
    });

    it('connects to SSE on mount and handles messages', async () => {
        render(<App />);

        expect(global.EventSource).toHaveBeenCalledWith(expect.stringContaining('/api/stream'));

        // Simulate incoming message
        const newsItem = {
            id: '1',
            headline: 'Test Headline',
            summary: 'Test Summary',
            sentiment: 'BULLISH',
            score: 8,
            url: 'http://test.com',
            source: 'Test Source',
            timestamp: new Date().toISOString()
        };

        act(() => {
            mockEventSource.onmessage({ data: JSON.stringify(newsItem) });
        });

        await waitFor(() => {
            expect(screen.getByText('Test Headline')).toBeInTheDocument();
        });

        expect(screen.getByText(/🟢 Bullish/i)).toBeInTheDocument();
    });

    it('deduplicates news items', async () => {
        render(<App />);

        const newsItem1 = {
            id: '1',
            headline: 'Test Headline',
            summary: 'Test Summary',
            sentiment: 'BULLISH',
            score: 8,
            url: 'http://test.com',
            source: 'Test Source',
            timestamp: new Date().toISOString()
        };

        const newsItem2 = {
            ...newsItem1,
            id: '2' // Different ID/Key, but same URL
        };

        // Send twice
        act(() => {
            mockEventSource.onmessage({ data: JSON.stringify(newsItem1) });
            mockEventSource.onmessage({ data: JSON.stringify(newsItem2) });
        });

        await waitFor(() => {
            expect(screen.getAllByText('Test Headline')).toHaveLength(1);
        });
    });

    it('displays correct badges for different sentiments', async () => {
        render(<App />);

        const bearishItem = {
            id: '2',
            headline: 'Bearish News',
            summary: 'Summary',
            sentiment: 'BEARISH',
            score: 2,
            url: 'http://bear.com',
            source: 'Source',
            timestamp: new Date().toISOString()
        };

        const neutralItem = {
            id: '3',
            headline: 'Neutral News',
            summary: 'Summary',
            sentiment: 'NEUTRAL',
            score: 5,
            url: 'http://neutral.com',
            source: 'Source',
            timestamp: new Date().toISOString()
        };

        act(() => {
            mockEventSource.onmessage({ data: JSON.stringify(bearishItem) });
            mockEventSource.onmessage({ data: JSON.stringify(neutralItem) });
        });

        await waitFor(() => {
            expect(screen.getByText(/🔴 Bearish/i)).toBeInTheDocument();
            expect(screen.getByText(/🟡 Neutral/i)).toBeInTheDocument();
        });
    });

    it('handles start feed button click', async () => {
        global.fetch.mockResolvedValueOnce({ ok: true });
        render(<App />);

        const startButton = screen.getByText('Start Feed');
        fireEvent.click(startButton);

        expect(global.fetch).toHaveBeenCalledWith('/api/feed/start', { method: 'POST' });
    });

    it('handles stop feed button click', async () => {
        global.fetch.mockResolvedValueOnce({ ok: true });
        render(<App />);

        const stopButton = screen.getByText('Stop Feed');
        fireEvent.click(stopButton);

        expect(global.fetch).toHaveBeenCalledWith('/api/feed/stop', { method: 'POST' });
    });

    it('handles SSE error', () => {
        render(<App />);

        act(() => {
            mockEventSource.onerror(new Event('error'));
        });

        expect(screen.getByText(/Disconnected/i)).toBeInTheDocument();
    });

    it('handles connection open state', async () => {
        render(<App />);
        act(() => {
            mockEventSource.onopen();
        });
        expect(screen.getByText(/Connected ✅/i)).toBeInTheDocument();
    });

    it('renders news item without URL', async () => {
        render(<App />);
        const itemNoUrl = {
            id: '99',
            headline: 'No URL News',
            summary: 'Summary',
            sentiment: 'NEUTRAL',
            score: 5,
            source: 'Source',
            timestamp: new Date().toISOString()
        };

        act(() => {
            mockEventSource.onmessage({ data: JSON.stringify(itemNoUrl) });
        });

        await waitFor(() => {
            expect(screen.getByText('No URL News')).toBeInTheDocument();
        });

        // Ensure regular link is not present for this item
        // Use queryByText to assert absence if needed, or check link count
    });
});
