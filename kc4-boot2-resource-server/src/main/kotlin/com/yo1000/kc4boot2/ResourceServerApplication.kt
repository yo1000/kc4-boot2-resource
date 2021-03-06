package com.yo1000.kc4boot2

import org.keycloak.adapters.AdapterDeploymentContext
import org.keycloak.adapters.springsecurity.AdapterDeploymentContextFactoryBean
import org.keycloak.adapters.springsecurity.KeycloakConfiguration
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@SpringBootApplication
class ResourceServerApplication

fun main(args: Array<String>) {
    runApplication<ResourceServerApplication>(*args)
}

@KeycloakConfiguration
class SecurityConfig : KeycloakWebSecurityConfigurerAdapter() {
    override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy = NullAuthenticatedSessionStrategy()

    override fun adapterDeploymentContext(): AdapterDeploymentContext = AdapterDeploymentContextFactoryBean(
            ClassPathResource("keycloak.json")).also { it.afterPropertiesSet() }.`object`!!

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!.authenticationProvider(keycloakAuthenticationProvider().also {
            it.setGrantedAuthoritiesMapper(SimpleAuthorityMapper().also {
                it.setConvertToUpperCase(true)
            })
        })
    }

    override fun configure(http: HttpSecurity) {
        super.configure(http)
        http.authorizeRequests()
                .antMatchers("/sso/login*").permitAll()
                .antMatchers("/api/customers*").hasAnyRole("USER")
                .antMatchers("/api/admin*").hasAnyRole("ADMIN")
                .anyRequest().permitAll()
        http.csrf().disable()
        http.cors().configurationSource(
            UrlBasedCorsConfigurationSource().also {
                it.registerCorsConfiguration("/**", CorsConfiguration().also {
                    it.addAllowedHeader(CorsConfiguration.ALL)
                    it.addAllowedMethod(HttpMethod.GET.name)
                    it.addAllowedOrigin("http://localhost:8081")
                    it.allowCredentials = true
                })
            }
        )
    }
}

@RestController
@RequestMapping("/api")
class Kc4Controller {
    @GetMapping("/customers")
    fun getCustomers(token: KeycloakAuthenticationToken): Any = mapOf(
            "content" to "Customers content"
    )

    @GetMapping("/admin")
    fun getAdmin(token: KeycloakAuthenticationToken): Any = mapOf(
            "content" to "Admin content"
    )
}