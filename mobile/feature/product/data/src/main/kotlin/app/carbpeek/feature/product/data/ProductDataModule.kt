package app.carbpeek.feature.product.data

import app.carbpeek.core.common.APPLICATION_SCOPE
import app.carbpeek.feature.product.api.ProductRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val productDataModule = module {
    singleOf(::ProductPersistenceImpl) bind ProductPersistence::class
    single<ProductRepository> { ProductRepositoryImpl(get(), get(), get(), get(), get(named(APPLICATION_SCOPE))) }
}
