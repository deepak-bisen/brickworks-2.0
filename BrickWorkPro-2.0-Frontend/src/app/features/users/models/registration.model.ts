export interface BaseRegistration {
  username: string;
  email: string;
  password: string;
  fullName: string;
  phoneNumber: string;
}

export interface CustomerRegistration extends BaseRegistration {
  address: string;
  customerType: 'INDIVIDUAL' | 'CONTRACTOR'; // Matches CustomerType enum
}

export interface EmployeeRegistration extends BaseRegistration {
  employeeCode: string;
  designation: string;
}
