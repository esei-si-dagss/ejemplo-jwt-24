package es.uvigo.mei.jwt.entidades;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "USUARIO")
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String login;

	private String password;

	private String nombre;

	private String email;

	@ElementCollection
	@CollectionTable(name = "ROL")
	@Enumerated(EnumType.STRING)
	private Set<Rol> roles = new HashSet<>();

	public Usuario() {

	}

	public Usuario(String login, String password, String nombre, String email) {
		super();
		this.login = login;
		this.password = password;
		this.nombre = nombre;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<Rol> getRoles() {
		return roles;
	}

	public void setRoles(Set<Rol> roles) {
		this.roles = roles;
	}

	public void anadirRol(Rol rol) {
		this.roles.add(rol);
	}

	public void eliminarRol(Rol rol) {
		this.roles.remove(rol);
	}

	@Override
	public String toString() {
		return "Usuario [id=" + id + ", login=" + login + ", password=" + password + ", email=" + email + ", roles="
				+ roles + "]";
	}

	
	
}
