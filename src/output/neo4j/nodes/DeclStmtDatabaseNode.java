package output.neo4j.nodes;

import java.util.HashMap;
import java.util.Map;

import astnodes.statements.IdentifierDeclStatement;

public class DeclStmtDatabaseNode extends DatabaseNode {

	String typeStr;
	String idStr;
	
	@Override
	public void initialize(Object obj)
	{
		IdentifierDeclStatement stmt = (IdentifierDeclStatement) obj;
		typeStr = stmt.getTypeAsString();
	
	}

	@Override
	public Map<String, Object> createProperties()
	{	
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("type", "DeclStmt");
		return map;
	}

}
