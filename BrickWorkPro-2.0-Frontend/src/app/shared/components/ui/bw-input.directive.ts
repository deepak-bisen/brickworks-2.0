import { Directive, HostBinding } from '@angular/core';

@Directive({
  selector: 'input[bwInput], select[bwInput], textarea[bwInput]',
  standalone: true,
})
export class BwInputDirective {
  @HostBinding('class')
  readonly hostClass =
    'bw-input block w-full px-4 py-2.5 text-sm text-gray-900 bg-gray-50 border border-gray-200 rounded-xl outline-none transition-all focus:bg-white focus:border-red-600 focus:ring-2 focus:ring-red-600/20';
}