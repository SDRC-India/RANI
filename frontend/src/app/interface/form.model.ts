export class FormModel<T> {
    value?: T;
    key: string;
    label: string;
    required: boolean;
    controlType: string;
    type?: string;
    placeholder?: string;
    keyHeading?: string;
    options?: {key: string, value: string}[];

    constructor(options: {
        value?: T,
        key?: string,
        label?: string,
        required?: boolean,
        order?: number,
        placeholder?: string,
        keyHeading?: string,
        controlType?: string
        } = {}) {
        this.value = options.value;
        this.key = options.key || '';
        this.label = options.label || '';
        this.required = !!options.required;
        this.controlType = options.controlType || '';
        if(this.controlType == 'textbox'){
            this.type = 'textbox';
        }
        if(this.controlType == 'dropdown' || this.controlType == 'radio'){
            this.options = options['options'] || [];
        }
    }
}
// export class TextboxQuestion extends FormModel<string> {
//     controlType = 'textbox';
//     type: string;
  
//     constructor(options: {} = {}) {
//       super(options);
//       this.type = options['type'] || '';
//     }
// }
// export class DropdownQuestion extends FormModel<string> {
//     controlType = 'dropdown';
//     options: {key: string, value: string}[] = [];
  
//     constructor(options: {} = {}) {
//       super(options);
//       this.options = options['options'] || [];
//     }
//   }