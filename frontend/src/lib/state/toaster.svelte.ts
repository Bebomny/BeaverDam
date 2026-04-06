export type ToastType = 'info' | 'success' | 'warning' | 'error'

export interface Toast {
    id: string;
    message: string;
    type: ToastType;
}

class ToastManager {
    toasts = $state<Toast[]>([]);

    add(message: string, type: ToastType = 'info', durationMs: number = 3000) {
        const id = crypto.randomUUID();

        this.toasts.push({ id, message, type });

        setTimeout(() => {
            this.remove(id);
        }, durationMs);
    }

    remove(id: string) {
        this.toasts = this.toasts.filter(toast => toast.id !== id);
    }
}

export const toaster = new ToastManager();