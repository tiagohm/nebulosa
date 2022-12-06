import { Injectable } from '@angular/core'

@Injectable({ providedIn: 'root' })
export class StorageService {

  string(key: string, value?: string) {
    if (value === undefined) return localStorage.getItem(key) || undefined
    else localStorage.setItem(key, value)
  }

  number(key: string, value?: number) {
    if (value === undefined) {
      const item = localStorage.getItem(key)
      return item ? parseFloat(item) : undefined
    }
    else localStorage.setItem(key, value?.toString())
  }

  bool(key: string, value?: boolean) {
    if (value === undefined) {
      const item = localStorage.getItem(key)
      return item ? item === 'true' : undefined
    }
    else localStorage.setItem(key, value ? 'true' : 'false')
  }

  json<T>(key: string, value?: T) {
    if (value === undefined) return JSON.parse(localStorage.getItem(key)) as T
    else localStorage.setItem(key, JSON.stringify(value))
  }
}
