package com.yo1000.kc4boot2

import org.keycloak.adapters.AdapterDeploymentContext
import org.keycloak.adapters.springsecurity.AdapterDeploymentContextFactoryBean
import org.keycloak.adapters.springsecurity.KeycloakConfiguration
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ClassPathResource
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class ResourceClientApplication

fun main(args: Array<String>) {
    runApplication<ResourceClientApplication>(*args)
}

@KeycloakConfiguration
class SecurityConfig : KeycloakWebSecurityConfigurerAdapter() {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun restTemplate(factory: KeycloakClientRequestFactory): RestTemplate {
        return KeycloakRestTemplate(factory)
    }

    override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return RegisterSessionAuthenticationStrategy(SessionRegistryImpl())
    }

    override fun adapterDeploymentContext(): AdapterDeploymentContext {
        val factoryBean = AdapterDeploymentContextFactoryBean(ClassPathResource("keycloak.json"))
        factoryBean.afterPropertiesSet()
        return factoryBean.`object`!!
    }

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
                .antMatchers("/customers*").hasAnyRole("USER")
                .antMatchers("/admin*").hasAnyRole("ADMIN")
                .anyRequest().permitAll()
        http.csrf().disable()
    }
}

@RestController
class Kc4Controller(
        val restTemplate: RestTemplate
) {
    @GetMapping("/customers")
    fun getCustomers(token: KeycloakAuthenticationToken): Any {
        val url = "http://localhost:8082/api/customers"
        val resp = restTemplate.getForObject(url, String::class.java)

        return """
            <h1>customers</h1>
            <p><code>
            ${token}
            </code></p>
            <ul>
            <li><code>${token.name}</code></li>
            <li><code>${token.account.keycloakSecurityContext.token.preferredUsername}</code></li>
            </ul>
            <h2>API Response</h2>
            <code>GET ${url}<code>
            <p><code>
            ${resp}
            </code></p>
            """.trimIndent()
    }

    @GetMapping("/admin")
    fun getAdmin(token: KeycloakAuthenticationToken): Any {
        val url = "http://localhost:8082/api/admin"
        val resp = restTemplate.getForObject(url, String::class.java)

        return """
            <h1>admin</h1>
            <p><code>
            ${token}
            </code></p>
            <ul>
            <li><code>${token.name}</code></li>
            <li><code>${token.account.keycloakSecurityContext.token.preferredUsername}</code></li>
            </ul>
            <h2>API Response</h2>
            <code>GET ${url}<code>
            <p><code>
            ${resp}
            </code></p>
            """.trimIndent()
    }
}
