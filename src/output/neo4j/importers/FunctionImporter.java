package output.neo4j.importers;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

import output.neo4j.EdgeTypes;
import output.neo4j.batchInserter.Neo4JBatchInserter;
import output.neo4j.nodes.ASTPseudoNode;
import output.neo4j.nodes.CFGPseudoNode;
import output.neo4j.nodes.FileDatabaseNode;
import output.neo4j.nodes.FunctionDatabaseNode;
import astnodes.ASTNode;
import astnodes.functionDef.FunctionDef;
import cfg.BasicBlock;
import cfg.CFG;

// Stays alive while importing a function into
// the database

public class FunctionImporter extends ASTNodeImporter
{
	ASTImporter astImporter = new ASTImporter(nodeStore);
	CFGImporter cfgImporter = new CFGImporter(nodeStore);
	
	public void addToDatabaseSafe(ASTNode node)
	{
		try{
			FunctionDatabaseNode function = new FunctionDatabaseNode();
			function.initialize(node);
			addFunctionToDatabase(function);
			linkFunctionToFileNode(function, curFile);
		}catch(RuntimeException ex)
		{
			ex.printStackTrace();
			System.err.println("Error adding function to database: " + ((FunctionDef) node).name.getEscapedCodeStr());
			return;
		}
	}
	
	private void addFunctionToDatabase(FunctionDatabaseNode function)
	{
		
		addMainNode(function);
		
		// and add the pseudo nodes
		nodeStore.addNeo4jNode(function.getASTPseudoNode(), null);
		nodeStore.addNeo4jNode(function.getCFGPseudoNode(), null);
		
		astImporter.setCurrentFunction(function);
		cfgImporter.setCurrentFunction(function);
		
		astImporter.addASTToDatabase(function.getASTRoot());
		cfgImporter.addCFGToDatabase(function.getCFG());	
		
		linkFunctionToASTAndCFG(function);
	
	}

	private void linkFunctionToASTAndCFG(FunctionDatabaseNode function)
	{
		
		linkFunctionWithASTPseudoNode(function);
		
		linkPseudoASTWithRootASTNode(function.getASTPseudoNode(), function.getASTRoot());
		
		CFG cfg = function.getCFG();
		if(cfg != null){
			linkFunctionWithCFGPseudoNode(function);
			linkPseudoCFGWithRootCFGNode(function.getCFGPseudoNode(), cfg);
		}
	}
	
	private void linkPseudoCFGWithRootCFGNode(CFGPseudoNode cfgPseudoNode, CFG cfg)
	{
		RelationshipType rel = DynamicRelationshipType.withName(EdgeTypes.IS_CFG_OF_CFG_ROOT);

		BasicBlock firstBlock = cfg.getFirstBlock();
		if(firstBlock == null){
			// TODO: sometimes there is not a single block in the CFG.
			// It's probably fine but it needs to be investigated.
			return;
		}
		
		long functionId = nodeStore.getIdForObject(cfgPseudoNode);
		long cfgRootId = nodeStore.getIdForObject(firstBlock);
		
		Neo4JBatchInserter.addRelationship(functionId, cfgRootId, rel, null);
	
	}

	private void linkFunctionWithASTPseudoNode(FunctionDatabaseNode function)
	{
		RelationshipType rel = DynamicRelationshipType.withName(EdgeTypes.IS_FUNCTION_OF_AST);
		
		long functionId = nodeStore.getIdForObject(function);
		long pseudoNodeId = nodeStore.getIdForObject(function.getASTPseudoNode());
		
		Neo4JBatchInserter.addRelationship(functionId, pseudoNodeId, rel, null);
	}
	
	private void linkFunctionWithCFGPseudoNode(FunctionDatabaseNode function)
	{
		RelationshipType rel = DynamicRelationshipType.withName(EdgeTypes.IS_FUNCTION_OF_CFG);
		
		long functionId = nodeStore.getIdForObject(function);
		long pseudoNodeId = nodeStore.getIdForObject(function.getCFGPseudoNode());
		
		Neo4JBatchInserter.addRelationship(functionId, pseudoNodeId, rel, null);
		
	}
	
	private void linkPseudoASTWithRootASTNode(ASTPseudoNode astPseudoNode, ASTNode astRoot)
	{
		RelationshipType rel = DynamicRelationshipType.withName(EdgeTypes.IS_AST_OF_AST_ROOT);
		
		long functionId = nodeStore.getIdForObject(astPseudoNode);
		long astRootId = nodeStore.getIdForObject(astRoot);
		
		Neo4JBatchInserter.addRelationship(functionId, astRootId, rel, null);
	}
	
	private void linkFunctionToFileNode(FunctionDatabaseNode function,
			FileDatabaseNode fileNode)
	{
		RelationshipType rel = DynamicRelationshipType.withName(EdgeTypes.IS_FILE_OF);
		
		long fileId = fileNode.getId();
		long functionId = nodeStore.getIdForObject(function);
		
		Neo4JBatchInserter.addRelationship(fileId, functionId, rel, null);
	}
	
}
