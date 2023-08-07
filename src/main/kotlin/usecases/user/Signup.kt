package usecases.user

import entities.user.UserValidations
import entities.user.Users
import org.ktapi.db.transaction
import org.ktapi.entities.Validation.validEmailDomain
import org.ktapi.entities.Validation.validateField
import services.EmailService

object Signup {
    fun signup(email: String, firstName: String = "", lastName: String = "") = transaction {
        val user = Users.findByEmail(email)

        if (user == null) {
            validateField("email", email) { validEmailDomain() }
            
            val validation = UserValidations.createForEmailValidation(email, firstName, lastName)

            EmailService.sendEmailVerification(validation)

            validation
        } else {
            ForgotPassword.forgotPassword(user)
        }
    }
}