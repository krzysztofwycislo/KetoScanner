package pl.morning.tower.karbo.feature.product.data

import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.morning.tower.karbo.core.common.APPLICATION_SCOPE
import pl.morning.tower.karbo.feature.product.api.ProductRepository

val productDataModule = module {
    singleOf(::ProductPersistenceImpl) bind ProductPersistence::class
    single<ProductRepository> { ProductRepositoryImpl(get(), get(), get(), get(), get(named(APPLICATION_SCOPE))) }
}
