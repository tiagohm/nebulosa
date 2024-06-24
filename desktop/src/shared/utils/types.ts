export type Undefinable<T> = T | undefined

export type Nullable<T> = Undefinable<T> | null
