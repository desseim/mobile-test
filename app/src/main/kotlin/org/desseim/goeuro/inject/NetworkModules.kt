package org.desseim.goeuro.inject

import android.location.Location
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dagger.Module
import dagger.Provides
import org.desseim.goeuro.BuildConfig
import org.desseim.goeuro.rest.ApiGeoLocationToAndroidLocationConverter
import org.desseim.goeuro.rest.PlaceService
import retrofit.Endpoint
import retrofit.Endpoints
import retrofit.RestAdapter
import retrofit.converter.Converter
import retrofit.converter.JacksonConverter
import javax.inject.Singleton

@Module
object GoEuroApiModule {
    @Provides @Singleton fun provideRestAdapter(apiHost: Endpoint, converter: Converter): RestAdapter {
        val builder = RestAdapter.Builder()
                .setEndpoint(apiHost)
                .setConverter(converter)
        if (BuildConfig.DEBUG) builder.setLogLevel(RestAdapter.LogLevel.FULL)
        return builder.build()
    }

    @Provides fun provideApiResponseConverter(defaultObjectMapper: ObjectMapper): Converter =
            JacksonConverter(defaultObjectMapper)

    @Provides fun provideDefaultJacksonObjectMapper(): ObjectMapper {
        val module = SimpleModule()
        module.addDeserializer(Location::class.java, StdDelegatingDeserializer<Location>(ApiGeoLocationToAndroidLocationConverter()))

        return ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .registerModule(module)
                .registerModule(KotlinModule())
    }

    @Provides fun provideApiHost(): Endpoint =
            GoEuroApiConfig.API_HOST
}

@Module(includes = arrayOf(GoEuroApiModule::class))
object GoEuroApiSearchModule {
    @Provides @Singleton fun provideLocationService(restAdapter: RestAdapter): PlaceService =
            restAdapter.create(javaClass<PlaceService>())
}

private object GoEuroApiConfig {
    val API_HOST = Endpoints.newFixedEndpoint("http://api.goeuro.com/api")
}