import { FormControl } from '@angular/forms'

export function validate(controls: FormControl[]) {
  controls.forEach((item) => item.updateValueAndValidity())
  const invalid = controls.filter((item) => item.invalid)
  invalid.forEach((item) => item.markAsTouched())
  return invalid.length <= 0
}
