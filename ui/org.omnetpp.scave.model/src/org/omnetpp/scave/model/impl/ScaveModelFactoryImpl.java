/**
 */
package org.omnetpp.scave.model.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.omnetpp.scave.model.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ScaveModelFactoryImpl extends EFactoryImpl implements ScaveModelFactory {
    /**
     * Creates the default factory implementation.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static ScaveModelFactory init() {
        try {
            ScaveModelFactory theScaveModelFactory = (ScaveModelFactory)EPackage.Registry.INSTANCE.getEFactory(ScaveModelPackage.eNS_URI);
            if (theScaveModelFactory != null) {
                return theScaveModelFactory;
            }
        }
        catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new ScaveModelFactoryImpl();
    }

    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ScaveModelFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case ScaveModelPackage.ANALYSIS: return createAnalysis();
            case ScaveModelPackage.INPUTS: return createInputs();
            case ScaveModelPackage.INPUT_FILE: return createInputFile();
            case ScaveModelPackage.CHARTS: return createCharts();
            case ScaveModelPackage.PROPERTY: return createProperty();
            case ScaveModelPackage.BAR_CHART: return createBarChart();
            case ScaveModelPackage.LINE_CHART: return createLineChart();
            case ScaveModelPackage.HISTOGRAM_CHART: return createHistogramChart();
            case ScaveModelPackage.SCATTER_CHART: return createScatterChart();
            case ScaveModelPackage.FOLDER: return createFolder();
            case ScaveModelPackage.MATPLOTLIB_CHART: return createMatplotlibChart();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Analysis createAnalysis() {
        AnalysisImpl analysis = new AnalysisImpl();
        return analysis;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Inputs createInputs() {
        InputsImpl inputs = new InputsImpl();
        return inputs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public InputFile createInputFile() {
        InputFileImpl inputFile = new InputFileImpl();
        return inputFile;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Charts createCharts() {
        ChartsImpl charts = new ChartsImpl();
        return charts;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Property createProperty() {
        PropertyImpl property = new PropertyImpl();
        return property;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public BarChart createBarChart() {
        BarChartImpl barChart = new BarChartImpl();
        return barChart;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public LineChart createLineChart() {
        LineChartImpl lineChart = new LineChartImpl();
        return lineChart;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public HistogramChart createHistogramChart() {
        HistogramChartImpl histogramChart = new HistogramChartImpl();
        return histogramChart;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ScatterChart createScatterChart() {
        ScatterChartImpl scatterChart = new ScatterChartImpl();
        return scatterChart;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Folder createFolder() {
        FolderImpl folder = new FolderImpl();
        return folder;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MatplotlibChart createMatplotlibChart() {
        MatplotlibChartImpl matplotlibChart = new MatplotlibChartImpl();
        return matplotlibChart;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ScaveModelPackage getScaveModelPackage() {
        return (ScaveModelPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    @Deprecated
    public static ScaveModelPackage getPackage() {
        return ScaveModelPackage.eINSTANCE;
    }

} //ScaveModelFactoryImpl
