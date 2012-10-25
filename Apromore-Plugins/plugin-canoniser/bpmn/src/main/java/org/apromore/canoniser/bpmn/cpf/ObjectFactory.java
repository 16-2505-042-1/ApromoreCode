package org.apromore.canoniser.bpmn.cpf;

// Java 2 Standard packages
import javax.xml.bind.annotation.XmlRegistry;

// Local packages
import org.apromore.cpf.CanonicalProcessType;
import org.apromore.cpf.EdgeType;
import org.apromore.cpf.EventType;
import org.apromore.cpf.NetType;
import org.apromore.cpf.ObjectType;
import org.apromore.cpf.ResourceTypeType;
import org.apromore.cpf.TaskType;
import org.apromore.cpf.XORSplitType;

/**
 * Element factory for a CPF 1.0 object model with convenience methods.
 *
 * @author <a href="mailto:simon.raboczi@uqconnect.edu.au">Simon Raboczi</a>
 * @since 0.4
 */
@XmlRegistry
public class ObjectFactory extends org.apromore.cpf.ObjectFactory {

    @Override
    public CpfCanonicalProcessType createCanonicalProcessType() {
        return new CpfCanonicalProcessType();
    }

    @Override
    public CpfEdgeType createEdgeType() {
        return new CpfEdgeType();
    }

    @Override
    public CpfEventType createEventType() {
        return new CpfEventType();
    }

    @Override
    public CpfNetType createNetType() {
        return new CpfNetType();
    }

    @Override
    public CpfObjectType createObjectType() {
        return new CpfObjectType();
    }

    @Override
    public CpfResourceTypeType createResourceTypeType() {
        return new CpfResourceTypeType();
    }

    @Override
    public CpfTaskType createTaskType() {
        return new CpfTaskType();
    }

    @Override
    public CpfXORSplitType createXORSplitType() {
        return new CpfXORSplitType();
    }
}
