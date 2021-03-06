package com.daw.club.model.dao;

import com.daw.club.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ClienteDAOJDBC implements ClienteDAO {
    private static final String dbTable="Clientes";
    private static final String[] autoField={"id"}; //Autogenerated field for new records
    private static final String connPoolName="java:comp/env/jdbc/gestClub";  //Tomcat
    //private static String connPoolName="jdbc/gestClub";               //Glassfish
    private static final String SQL_BUSCAID="SELECT * FROM Clientes where id=?";
    private static final String SQL_BUSCATODOS="SELECT * FROM Clientes";
    private static final String SQL_CREA="INSERT INTO Clientes (nombre,dni,socio, mediopago) VALUES (?,?,?,?)";
    private static final String SQL_ACTUALIZA="UPDATE Clientes set NOMBRE=?, DNI=?, SOCIO=?, MEDIOPAGO=? WHERE id=?";
    private static final String SQL_BORRA="DELETE FROM Clientes WHERE id=?";

    private DataSource ds=null;
    
    public ClienteDAOJDBC() {
        if (ds==null) {
            try {
                Context context = new InitialContext();
                ds = (DataSource) context.lookup(connPoolName);
            } catch (NamingException ex) {
                Logger.getLogger(ClienteDAOJDBC.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
    
    /**Recupera un Cliente del registro actual del RS (MAPPING)*/
    private static Cliente clienteMapper(ResultSet rs) throws SQLException {
        Cliente c;
        c=new Cliente(  rs.getInt("id"),
                        rs.getString("NOMBRE"),
                        rs.getString("DNI"),
                        rs.getBoolean("SOCIO"),
                        rs.getInt("MEDIOPAGO")
        );
        return c;
    }  
   
    @Override
    public Cliente buscaId(Integer id) {
        Cliente c=null;
        try (Connection conn=ds.getConnection();
             PreparedStatement stmn=conn.prepareStatement(SQL_BUSCAID)) {
            stmn.setInt(1,id);
            try( ResultSet rs=stmn.executeQuery()) {
                rs.next();
                c=clienteMapper(rs);                
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClienteDAOJDBC.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return c;
    }

    @Override
    public List<Cliente> buscaTodos() {
        List<Cliente> l=new ArrayList<>();
        try (Connection conn=ds.getConnection();
            Statement stmn=conn.createStatement();
            ResultSet rs=stmn.executeQuery(SQL_BUSCATODOS);
        ){
            while (rs.next()) {
                l.add(clienteMapper(rs));
            }                
        } catch (Exception ex) {
            Logger.getLogger(ClienteDAOJDBC.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return l;
    }

    @Override
    public boolean crea(Cliente c) {
        boolean result=false;
        try (Connection conn=ds.getConnection();
            PreparedStatement stmn=conn.prepareStatement(SQL_CREA,autoField);
        ){
            stmn.setString(1,c.getNombre());
            stmn.setString(2,c.getDni());
            stmn.setBoolean(3, c.isSocio());
            stmn.setInt(4, c.getMedioPago());
            stmn.executeUpdate();
            try (ResultSet rs=stmn.getGeneratedKeys()) {
                //Get autogenerated field value
                if (rs!=null && rs.next()) {
                    int nuevoId=rs.getInt(1); //RS has only one field with key value
                    c.setId(nuevoId);
                }
            } catch (Exception ex) {
                Logger.getLogger(ClienteDAOJDBC.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            Logger.getLogger(ClienteDAOJDBC.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public boolean guarda(Cliente c) {
        boolean result=false;
        try (Connection conn=ds.getConnection();
            PreparedStatement stmn=conn.prepareStatement(SQL_ACTUALIZA);
        ){
            stmn.setString(1,c.getNombre());
            stmn.setString(2,c.getDni());
            stmn.setBoolean(3, c.isSocio());
            stmn.setInt(4, c.getMedioPago());
            stmn.setInt(5,c.getId());
            result=(stmn.executeUpdate()==1);
        } catch (Exception ex) {
            Logger.getLogger(ClienteDAOJDBC.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return result;
    }
    @Override    
    public boolean borra(Integer id) {
        boolean result=false;
        try (Connection conn=ds.getConnection();
            PreparedStatement stmn=conn.prepareStatement(SQL_BORRA);
        ){
            stmn.setInt(1,id);
            result=(stmn.executeUpdate()==1);
        } catch (Exception ex) {
            Logger.getLogger(ClienteDAOJDBC.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }         
        return result;
    }
}
// Closing recourses and catching exceptions correctly JDK<7!!!!
//		Connection con = null;
//		Statement stmt = null;
//		ResultSet rs = null;
//		try {
//			con = ds.getConnection();
//			stmt = con.createStatement();
//			rs = stmt.executeQuery("select empid, name from Employee");
//			while(rs.next()){
//				System.out.println("Employee ID="+rs.getInt("empid")+", Name="+rs.getString("name"));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}finally{
//				try {
//					if(rs != null) rs.close();
//					if(stmt != null) stmt.close();
//					if(con != null) con.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//		}
//	}