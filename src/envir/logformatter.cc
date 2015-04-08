//==========================================================================
//  LOGFORMATTER.CC - part of
//                     OMNeT++/OMNEST
//             Discrete System Simulation in C++
//
//  Author: Andras Varga
//
//==========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2008 Andras Varga
  Copyright (C) 2006-2008 OpenSim Ltd.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#include "logformatter.h"
#include "common/commonutil.h"
#include "omnetpp/cmodule.h"
#include "omnetpp/ccomponenttype.h"
#include "omnetpp/cconfiguration.h"
#include "omnetpp/chasher.h"
#include "omnetpp/simutil.h"

NAMESPACE_BEGIN

LogFormatter::LogFormatter(const char *format)
{
    parseFormat(format);
}

void LogFormatter::parseFormat(const char *format)
{
    formatParts.clear();
    adaptiveTabColumns.clear();
    char *current = const_cast<char *>(format);
    char *previous = current;
    bool conditional = false;
    while (true)
    {
        char ch = *current;
        if (ch == '\0')
        {
            if (previous != current) {
                addPart(CONSTANT_TEXT, previous, current, conditional);
                conditional = false;
            }
            break;
        }
        else if (ch == '%')
        {
            if (previous != current) {
                addPart(CONSTANT_TEXT, previous, current, conditional);
                conditional = false;
            }
            previous = current;
            current++;
            ch = *current;
            if (ch == '%') {
                addPart(CONSTANT_TEXT, previous, current, conditional);
                conditional = false;
            }
            else if (ch == '|') {
                addPart(ADAPTIVE_TAB, NULL, NULL, conditional);
                adaptiveTabColumns.push_back(0);
                conditional = false;
            }
            else if (ch == '>') {
                addPart(INDENT, NULL, NULL, conditional);
                conditional = false;
            }
            else if (ch == '?')
                conditional = true;
            else if ('0' <= ch && ch <= '9') {
                char *tail;
                int padding = strtol(current, &tail, 10);
                addPart(PADDING, NULL, NULL, conditional);
                formatParts[formatParts.size() - 1].padding = padding;
                current = tail - 1;
                conditional = false;
            }
            else {
                addPart(getDirective(ch), NULL, NULL, conditional);
                conditional = false;
            }
            previous = current + 1;
        }
        current++;
    }
}

LogFormatter::FormatDirective LogFormatter::getDirective(char ch)
{
    if (strchr("lcetgvanmosqNMOSQGRXYZpbdzuxyfiwWHIEUCKJL", ch) == NULL)
        throw cRuntimeError("Unknown log format character '%c'", ch);
    return (LogFormatter::FormatDirective) ch;
}

void LogFormatter::addPart(FormatDirective directive, char *textBegin, char *textEnd, bool conditional)
{
    FormatPart part;
    part.directive = directive;
    part.conditional = conditional;
    if (textBegin && textEnd)
        part.text = std::string(textBegin, textEnd - textBegin);
    formatParts.push_back(part);
}

bool LogFormatter::usesEventName()
{
    for (std::vector<FormatPart>::iterator it = formatParts.begin(); it != formatParts.end(); it++)
        if (it->directive == EVENT_OBJECT_NAME || it->directive == EVENT_OBJECT)
            return true;
    return false;
}

std::string LogFormatter::formatPrefix(cLogEntry *entry)
{
    bool lastPartEmpty = true;
    std::stringstream stream;
    int adaptiveTabIndex = 0;
    for (std::vector<FormatPart>::iterator it = formatParts.begin(); it != formatParts.end(); it++)
    {
        FormatPart& part = *it;
        if (part.directive == CONSTANT_TEXT && (!part.conditional || !lastPartEmpty))
            stream << part.text;
        lastPartEmpty = false;
        switch (part.directive)
        {
            // constant text is already done
            case CONSTANT_TEXT:
                break;
            case PADDING:
            {
                int count = part.padding - stream.str().size();
                if (count > 0)
                    stream << std::string(count, ' ');
                break;
            }
            case ADAPTIVE_TAB:
            {
                int col = stream.str().size();
                int& tabCol = adaptiveTabColumns[adaptiveTabIndex];
                if (tabCol <= col)
                    tabCol = col;
                else
                    stream << std::string(tabCol - col, ' ');
                adaptiveTabIndex++;
                break;
            }
            case INDENT:
            {
                int depth = cMethodCallContextSwitcher::getDepth();
                if (depth > 0)
                    stream << std::string(2*depth, ' ');
                break;
            }

            // log statement related
            case LOGLEVEL:
                stream << cLogLevel::getName(entry->loglevel); break;
            case LOGCATEGORY:
                if (entry->category) stream << entry->category; else lastPartEmpty = true; break;

            // current simulation state related
            case EVENT_NUMBER:
                stream << simulation.getEventNumber(); break;
            case SIMULATION_TIME:
                stream << simulation.getSimTime(); break;
            case FINGERPRINT:
                if (simulation.getHasher()) stream << simulation.getHasher()->str().c_str(); else lastPartEmpty = true; break;

            case EVENT_OBJECT_NAME:
                if (ev.getCurrentEventName()) stream << ev.getCurrentEventName(); else lastPartEmpty = true; break;
            case EVENT_OBJECT_CLASSNAME:
                if (ev.getCurrentEventClassName()) stream << ev.getCurrentEventClassName(); else lastPartEmpty = true; break;

            case EVENT_MODULE_NAME:
                if (ev.getCurrentEventModule()) stream << ev.getCurrentEventModule()->getFullName(); else lastPartEmpty = true; break;
            case EVENT_MODULE_FULLPATH:
                if (ev.getCurrentEventModule()) stream << ev.getCurrentEventModule()->getFullPath(); else lastPartEmpty = true; break;
            case EVENT_MODULE_CLASSNAME:
                if (ev.getCurrentEventModule()) stream << ev.getCurrentEventModule()->getClassName(); else lastPartEmpty = true; break;
            case EVENT_MODULE_NEDTYPE_SIMPLENAME:
                if (ev.getCurrentEventModule()) stream << ev.getCurrentEventModule()->getComponentType()->getName(); else lastPartEmpty = true; break;
            case EVENT_MODULE_NEDTYPE_QUALIFIEDNAME:
                if (ev.getCurrentEventModule()) stream << ev.getCurrentEventModule()->getComponentType()->getFullName(); else lastPartEmpty = true; break;

            case CONTEXT_COMPONENT_NAME:
                if (simulation.getContext()) stream << simulation.getContext()->getFullName(); else lastPartEmpty = true; break;
            case CONTEXT_COMPONENT_FULLPATH:
                if (simulation.getContext()) stream << simulation.getContext()->getFullPath(); else lastPartEmpty = true; break;
            case CONTEXT_COMPONENT_CLASSNAME:
                if (simulation.getContext()) stream << simulation.getContext()->getClassName(); else lastPartEmpty = true; break;
            case CONTEXT_COMPONENT_NEDTYPE_SIMPLENAME:
                if (simulation.getContext()) stream << simulation.getContext()->getComponentType()->getName(); else lastPartEmpty = true; break;
            case CONTEXT_COMPONENT_NEDTYPE_QUALIFIEDNAME:
                if (simulation.getContext()) stream << simulation.getContext()->getComponentType()->getFullName(); else lastPartEmpty = true; break;

            // simulation run related
            case CONFIGNAME:
                stream << simulation.getActiveEnvir()->getConfigEx()->getActiveConfigName(); break;
            case RUNNUMBER:
                stream << simulation.getActiveEnvir()->getConfigEx()->getActiveRunNumber(); break;

            case NETWORK_MODULE_CLASSNAME:
                stream << simulation.getSystemModule()->getClassName(); break;
            case NETWORK_MODULE_NEDTYPE_SIMPLENAME:
                stream << simulation.getNetworkType()->getName(); break;
            case NETWORK_MODULE_NEDTYPE_QUALIFIEDNAME:
                stream << simulation.getNetworkType()->getFullName(); break;

            // C++ source related
            case SOURCE_OBJECT_POINTER:
                if (entry->sourcePointer) stream << entry->sourcePointer; else lastPartEmpty = true; break;
            case SOURCE_OBJECT_NAME:
                if (entry->sourceObject) stream << entry->sourceObject->getFullName(); else lastPartEmpty = true; break;
            case SOURCE_OBJECT_FULLPATH:
                if (entry->sourceObject) stream << entry->sourceObject->getFullPath(); else lastPartEmpty = true; break;

            case SOURCE_COMPONENT_NEDTYPE_SIMPLENAME:
                if (entry->sourceComponent) stream << entry->sourceComponent->getComponentType()->getName(); else lastPartEmpty = true; break;
            case SOURCE_COMPONENT_NEDTYPE_QUALIFIEDNAME:
                if (entry->sourceComponent) stream << entry->sourceComponent->getComponentType()->getFullName(); else lastPartEmpty = true; break;

            case SOURCE_FILE:
                stream << entry->sourceFile; break;
            case SOURCE_LINE:
                stream << entry->sourceLine; break;

            case SOURCE_OBJECT_CLASSNAME:
                stream << (entry->sourceComponent ? entry->sourceComponent->getComponentType()->getName() :
                          (entry->sourceObject ? entry->sourceObject->getClassName() :
                          (entry->sourceClass ? opp_demangle_typename(entry->sourceClass) : ""))); break;
            case SOURCE_FUNCTION:
                stream << entry->sourceFunction; break;

            // operating system related
            case USERTIME:
                stream << (double)entry->userTime / (double)CLOCKS_PER_SEC; break;
            case WALLTIME:
            {
                // chop off newline at the end (no worries, this is slow anyway)
                time_t now = time(NULL);
                std::string nowstr(ctime(&now));
                stream << nowstr.substr(0, nowstr.length() - 1); break;
            }

            case HOSTNAME:
                stream << opp_gethostname(); break;
            case PROCESSID:
                stream << getpid(); break;

            // compound fields
            case EVENT_OBJECT: {
                if (ev.getCurrentEventName())
                    stream << "(" << ev.getCurrentEventClassName() << ")" << ev.getCurrentEventName();
                else
                    lastPartEmpty = true;
                break;
            }
            case EVENT_MODULE: {
                cModule *mod = ev.getCurrentEventModule();
                if (mod)
                    stream << "(" << mod->getComponentType()->getName() << ")" << mod->getFullPath();
                else
                    lastPartEmpty = true;
                break;
            }
            case CONTEXT_COMPONENT_IF_DIFFERENT:
                if (simulation.getContext() == ev.getCurrentEventModule()) {
                    lastPartEmpty = true;
                    break;
                }
                // no break
            case CONTEXT_COMPONENT: {
                cComponent *component = simulation.getContext();
                if (component)
                    stream << "(" << component->getComponentType()->getName() << ")" << component->getFullPath();
                else
                    lastPartEmpty = true;
                break;
            }
            case SOURCE_COMPONENT_OR_OBJECT_IF_DIFFERENT:
                if (entry->sourceComponent == simulation.getContext()) {
                    lastPartEmpty = true;
                    break;
                }
                // no break
            case SOURCE_COMPONENT_OR_OBJECT: {
                if (entry->sourceComponent)
                    stream << "(" << entry->sourceComponent->getComponentType()->getName() << ")" << entry->sourceComponent->getFullPath();
                else if (entry->sourceObject) {
                    stream << "(" << entry->sourceClass << ")" <<
                        (entry->sourceObject->getOwner() == simulation.getContext() ?
                        entry->sourceObject->getFullName() : entry->sourceObject->getFullPath());
                }
                else if (entry->sourceClass || entry->sourcePointer) {
                    if (entry->sourceClass)
                        stream << "(" << entry->sourceClass << ")";
                    if (entry->sourcePointer)
                        stream << "0x" << std::hex << entry->sourcePointer;
                }
                else
                    lastPartEmpty = true;
                break;
            }

            default:
                throw cRuntimeError("Unknown format directive '%d'", part.directive);
        }
    }
    return stream.str();
}

void LogFormatter::resetAdaptiveTabs()
{
    for (size_t i = 0; i < adaptiveTabColumns.size(); i++)
        adaptiveTabColumns[i] = 0;
}

NAMESPACE_END