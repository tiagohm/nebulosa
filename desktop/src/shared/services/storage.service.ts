export interface StorageService {

    clear(): void

    delete(key: string): void

    get<T>(key: string, defaultValue: T): Promise<T> | T

    has(key: string): Promise<boolean> | boolean

    set(key: string, value: any): void
}