import { EventSource } from 'extended-eventsource';
export interface BackupData {
    id: number;
    name: string;
    status: string;
    deleteTime: string | null;
    user: string;
}

export class SseService {
    private eventSource: EventSource | null = null;
    private listeners: { [key: string]: (data: BackupData) => void } = {};

    connect(url: string = '/api/sse') {
        if (this.eventSource) return;
        this.eventSource = new EventSource(
            url, {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem('access_token')}`,
                }
            }
        );

        // Listen for the custom 'sse-event' type
        this.eventSource.addEventListener('sse-event', (event: MessageEvent) => {
            try {
                const data: BackupData = JSON.parse(event.data);
                // Notify all registered listeners
                Object.values(this.listeners).forEach((listener) => listener(data));
            } catch (error) {
                console.error('Error parsing SSE data:', error);
            }
        });

        // Handle connection open
        this.eventSource.onopen = () => {
            console.log('SSE connection opened');
        };

        // Handle errors (e.g., connection lost)
        this.eventSource.onerror = (error) => {
            console.error('SSE connection error:', error);
            // Optionally reconnect after a delay
            setTimeout(() => this.reconnect(url), 5000);
        };
    }

    // Register a listener for SSE events
    onEvent(callback: (data: BackupData) => void) {
        const id = Date.now().toString(); // Simple unique ID
        this.listeners[id] = callback;
        return id; // Return ID to allow removal
    }

    // Remove a listener
    offEvent(id: string) {
        delete this.listeners[id];
    }

    // Disconnect
    disconnect() {
        if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
        }
        this.listeners = {};
    }

    // Reconnect (for error recovery)
    private reconnect(url: string) {
        this.disconnect();
        this.connect(url);
    }
}

// Export a singleton instance
export const sseService = new SseService();