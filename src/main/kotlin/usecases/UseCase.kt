package usecases

import entities.organization.OrganizationUser
import entities.organization.OrganizationUsers
import entities.organization.UserRole
import entities.user.User
import entities.user.UserLogin
import org.ktlib.entities.UnauthorizedException
import org.ktlib.typeArguments
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

enum class Role {
    Owner, Admin, User, UserNoOrg, Employee, Anyone;

    val userRole: UserRole? by lazy { UserRole.entries.find { it.name == name } }
}

class UseCaseContext<T>(
    val userLogin: UserLogin? = null,
    val orgId: String? = null,
    val input: T? = null
)

typealias DataMap = Map<String, Any?>

abstract class UseCase<D : Any, T>(role: Role, vararg roles: Role) {
    companion object {
        fun <U : UseCase<D, T>, D : Any?, T : Any?> create(type: KClass<U>, context: UseCaseContext<D>): U {
            return type.createInstance().apply { this.context = context }
        }
    }

    val useCaseRoles: List<Role>
    private lateinit var context: UseCaseContext<D>

    val input: D get() = context.input!!
    val currentUserLoginOrNull: UserLogin? get() = context.userLogin
    val currentUserLogin: UserLogin get() = currentUserLoginOrNull!!
    val currentUserIdOrNull: String? get() = currentUserLoginOrNull?.userId
    val currentUserId: String get() = currentUserIdOrNull!!
    val currentUser: User get() = currentUserLogin.user
    val orgIdOrNull: String? get() = context.orgId
    val orgId: String get() = orgIdOrNull!!
    open val aliases: List<String> = emptyList()

    val currentOrganizationUser: OrganizationUser? by lazy {
        OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, orgId)
    }

    val userRole: UserRole get() = currentOrganizationUser!!.role

    @Suppress("UNCHECKED_CAST")
    val inputType: KClass<D> get() = typeArguments(UseCase::class)[0] as KClass<D>

    init {
        this.useCaseRoles = listOf(role, *roles)
    }

    private val notInitializedMessage =
        "UseCase context needs to be initialized before executing. Use UseCase.create() to create a UseCase instance."

    fun execute() = when {
        !::context.isInitialized -> throw IllegalStateException(notInitializedMessage)
        !roleAuthorize() -> throw UnauthorizedException()
        else -> doExecute()
    }

    protected abstract fun doExecute(): T

    private fun roleAuthorize() = when {
        useCaseRoles.contains(Role.Employee) -> context.userLogin != null && currentUser.employee
        useCaseRoles.contains(Role.Anyone) -> true
        useCaseRoles.contains(Role.UserNoOrg) && context.userLogin != null -> true
        context.userLogin != null && context.orgId != null -> userHasRole() && authorize()
        else -> false
    }

    open fun authorize() = true

    private fun userHasRole() =
        currentOrganizationUser?.hasPermission(useCaseRoles.mapNotNull { it.userRole }) == true

    protected fun <D : Any, T> executeUseCase(type: KClass<out UseCase<D, T>>, input: D): T {
        return create(type, UseCaseContext(context.userLogin, context.orgId, input)).execute()
    }
}