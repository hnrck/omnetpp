%module nedxml

// covariant return type warning disabled
#pragma SWIG nowarn=822

%include "loadlib.i"
%include "std_string.i"
%include "enums.swg"
%include "commondefs.i"

%javaconst(1);

%{
#include "nedelement.h"
#include "nederror.h"
#include "nedparser.h"
#include "ned2generator.h"
#include "ned1generator.h"
#include "xmlgenerator.h"
#include "nedelements.h"

#include "neddtdvalidator.h"
#include "nedtools.h"
#include "nedsyntaxvalidator.h"

// #include "nedcompiler.h"
#include "nedfilebuffer.h"
// #include "nedsemanticvalidator.h"
// #include "cppgenerator.h"
%}

// hide export/import macros from swig
#define COMMON_API
#define NEDXML_API
#define OPP_DLLEXPORT
#define OPP_DLLIMPORT

#define NAMESPACE_BEGIN
#define NAMESPACE_END
#define USING_NAMESPACE

namespace std {
//class ostream;
};

// parse functions return new objects, supposed to be deleted from Java
%newobject NEDParser::parseNEDText(const char *);
%newobject NEDParser::parseNEDFile(const char *);
%newobject NEDParser::parseNEDExpression(const char *);
%newobject createElementWithTag(int tagcode, NEDElement *parent);

// These are only public for technical reasons, shouldn't be wrapped
%ignore NEDParser::getSource();
%ignore NEDParser::getErrors();
%ignore NEDParser::error(const char *, int);

%ignore np;

%ignore NEDElementUserData;
%ignore NEDElement::setUserData(NEDElementUserData *);
%ignore NEDElement::getUserData() const;

%ignore generateNED1(std::ostream&, NEDElement *, NEDErrorStore *);
%ignore generateNED2(std::ostream&, NEDElement *, NEDErrorStore *);
%ignore generateXML(std::ostream&, NEDElement *, bool, int);
%ignore NEDInternalError;

// XXX for some reason, SWIG doesn't give a s&%$# about the following ignores:
%ignore NEDElementStore::add(NEDElement *, const char *, ...);
%ignore NEDElementStore::add(NEDElement *, int, const char *, ...);
%ignore NEDGenerator1::generate(std::ostream&, NEDElement *, const char *);
%ignore NEDGenerator2::generate(std::ostream&, NEDElement *, const char *);
%ignore NEDXMLGenerator::generate(std::ostream&, NEDElement *);

%ignore ltstr;

%ignore  FilesElement;
%ignore  NedFileElement;
%ignore  CommentElement;
%ignore  ImportElement;
%ignore  PropertyDeclElement;
%ignore  ExtendsElement;
%ignore  InterfaceNameElement;
%ignore  SimpleModuleElement;
%ignore  ModuleInterfaceElement;
%ignore  CompoundModuleElement;
%ignore  ChannelInterfaceElement;
%ignore  ChannelElement;
%ignore  ParametersElement;
%ignore  ParamGroupElement;
%ignore  ParamElement;
%ignore  PatternElement;
%ignore  PropertyElement;
%ignore  PropertyKeyElement;
%ignore  GatesElement;
%ignore  GateGroupElement;
%ignore  GateElement;
%ignore  TypesElement;
%ignore  SubmodulesElement;
%ignore  SubmoduleElement;
%ignore  ConnectionsElement;
%ignore  ConnectionElement;
%ignore  ChannelSpecElement;
%ignore  ConnectionGroupElement;
%ignore  LoopElement;
%ignore  ConditionElement;
%ignore  ExpressionElement;
%ignore  OperatorElement;
%ignore  FunctionElement;
%ignore  IdentElement;
%ignore  LiteralElement;
%ignore  MsgFileElement;
%ignore  NamespaceDeclElement;
%ignore  CplusplusElement;
%ignore  StructDeclElement;
%ignore  ClassDeclElement;
%ignore  MessageDeclElement;
%ignore  EnumDeclElement;
%ignore  EnumElement;
%ignore  EnumFieldsElement;
%ignore  EnumFieldElement;
%ignore  MessageElement;
%ignore  ClassElement;
%ignore  StructElement;
%ignore  FieldsElement;
%ignore  FieldElement;
%ignore  PropertiesElement;
%ignore  MsgpropertyElement;
%ignore  UnknownElement;


/* Let's just grab the original header file here */
%include "nedxmldefs.h"
%include "nedelement.h"
%include "nederror.h"
%include "nedparser.h"
%include "ned2generator.h"
%include "ned1generator.h"
%include "xmlgenerator.h"
%include "nedelements.h"

%include "nedvalidator.h"
%include "neddtdvalidatorbase.h"
%include "neddtdvalidator.h"
%include "nedtools.h"
%include "nedsyntaxvalidator.h"

//%include "nedcompiler.h"
//%include "nedfilebuffer.h"
//%include "nedsemanticvalidator.h"
//%include "cppgenerator.h"

