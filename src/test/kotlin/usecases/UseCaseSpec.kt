package usecases

import entities.TestData
import entities.user.UserLogin
import entities.user.UserLogins
import org.ktlib.test.EntitySpec
import kotlin.reflect.KClass

abstract class UseCaseSpec(body: EntitySpec.() -> Unit = {}) : EntitySpec(body) {
    lateinit var currentUserLogin: UserLogin
    val testOrganization = TestData.organization
    val currentUser = TestData.user
    val currentUserId = currentUser.id
    val testOrgId = testOrganization.id

    init {
        beforeEach {
            currentUserLogin = UserLogins.create(currentUserId)
        }
    }

    fun <D : Any, T> useCase(useCaseType: KClass<out UseCase<D, T>>, context: UseCaseContext<D>): UseCase<D, T> {
        return UseCase.create(useCaseType, context)
    }

    fun <D : Any, T> useCase(useCaseType: KClass<out UseCase<D, T>>, input: D): UseCase<D, T> {
        return UseCase.create(
            useCaseType,
            UseCaseContext(orgId = testOrgId, userLogin = currentUserLogin, input = input)
        )
    }
}