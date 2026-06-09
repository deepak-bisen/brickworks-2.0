// FIX: CustomerRegistrationDTO extends UserDTO which includes 'companyName' and 'gstNumber'
// for BUSINESS type customers. The frontend model was missing these fields,
// causing them to never be sent even when filled in the form.

export interface BaseRegistration {
  username: string;
  email: string;
  password: string;
  fullName: string;
  phoneNumber: string;
}

export interface CustomerRegistration extends BaseRegistration {
  customerType: 'INDIVIDUAL' | 'BUSINESS';
  billingAddress: string;
  // FIX: Added missing fields present in backend CustomerRegistrationDTO
  companyName?: string;
  gstNumber?: string;
}

export interface EmployeeRegistration extends BaseRegistration {
  // FIX: Backend EmployeeRegistrationDTO has both 'employeeCode' and 'shiftTiming'
  shiftTiming: string;
  employeeCode?: string;
}
