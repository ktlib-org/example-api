package usecases.employee

import usecases.Role
import usecases.UseCase

abstract class EmployeeUseCase<D : Any, T> : UseCase<D, T>(Role.Employee)