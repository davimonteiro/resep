/**
 * 
 */
package service.adaptation.effector;

import service.composite.CompositeService;

/**
 * Abstract effector, inherited with generic functions
 * @author Yifan Ruan
 * @email  ry222ad@student.lnu.se
 */
public class AbstractEffector {
	
	protected CompositeService compositeService;
	
	/**
	 * Constructor
	 * @param compositeService which composite service to be affected
	 */
	public AbstractEffector(CompositeService compositeService){
		this.compositeService=compositeService;
	}
	
	/**
	 * Get composite service
	 * @return the compositeService
	 */
	public CompositeService getCompositeService() {
		return compositeService;
	}

}
