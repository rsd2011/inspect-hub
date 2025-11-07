#!/usr/bin/env python3
"""
Module Dependency Validator for Spring Boot Multi-Module Project

Validates:
1. Module dependency rules (domain modules should not depend on server)
2. Circular dependency detection
3. Common module should not depend on domain modules
4. Package structure conventions

Usage:
    python scripts/validate-modules.py
"""

import re
import sys
from pathlib import Path
from collections import defaultdict, deque


class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    RESET = '\033[0m'


def success(msg):
    print(f"{Colors.GREEN}✅ {msg}{Colors.RESET}")


def error(msg):
    print(f"{Colors.RED}❌ ERROR: {msg}{Colors.RESET}")


def warning(msg):
    print(f"{Colors.YELLOW}⚠️  WARNING: {msg}{Colors.RESET}")


def info(msg):
    print(f"{Colors.BLUE}ℹ️  {msg}{Colors.RESET}")


class ModuleValidator:
    def __init__(self, backend_dir):
        self.backend_dir = Path(backend_dir)
        self.modules = {}
        self.dependencies = defaultdict(set)
        self.has_errors = False
        self.has_warnings = False

        # Module classification
        self.domain_modules = {'auth', 'policy', 'detection', 'investigation',
                               'reporting', 'batch', 'simulation', 'admin'}
        self.infrastructure_modules = {'common'}
        self.application_modules = {'server'}

    def discover_modules(self):
        """Discover all modules in the backend directory"""
        info("Discovering modules...")

        for item in self.backend_dir.iterdir():
            if item.is_dir() and not item.name.startswith('.'):
                build_gradle = item / 'build.gradle'
                if build_gradle.exists():
                    self.modules[item.name] = item
                    success(f"Found module: {item.name}")

        if not self.modules:
            error("No modules found")
            sys.exit(1)

    def parse_dependencies(self):
        """Parse dependencies from build.gradle files"""
        info("\nParsing module dependencies...")

        for module_name, module_path in self.modules.items():
            build_gradle = module_path / 'build.gradle'

            try:
                content = build_gradle.read_text(encoding='utf-8')

                # Find dependencies block
                deps_match = re.search(r'dependencies\s*\{([^}]*)\}', content, re.DOTALL)
                if not deps_match:
                    continue

                deps_block = deps_match.group(1)

                # Find project dependencies
                project_deps = re.findall(r"project\(['\"]:(backend|backend:(\w+))['\"]",
                                          deps_block)

                for match in project_deps:
                    if match[1]:  # backend:module format
                        dep_module = match[1]
                        self.dependencies[module_name].add(dep_module)

                if self.dependencies[module_name]:
                    deps_str = ', '.join(sorted(self.dependencies[module_name]))
                    print(f"  {module_name} → {deps_str}")

            except Exception as e:
                warning(f"Failed to parse {module_name}/build.gradle: {e}")

    def validate_dependency_rules(self):
        """Validate module dependency rules"""
        info("\nValidating dependency rules...")

        for module, deps in self.dependencies.items():
            # Rule 1: Domain modules should not depend on server
            if module in self.domain_modules and 'server' in deps:
                error(f"{module} (domain) depends on server (application) - FORBIDDEN")
                self.has_errors = True

            # Rule 2: Common should not depend on domain modules
            if module in self.infrastructure_modules:
                domain_deps = deps & self.domain_modules
                if domain_deps:
                    error(f"{module} (infrastructure) depends on domain modules {domain_deps} - FORBIDDEN")
                    self.has_errors = True

            # Rule 3: Server should aggregate domain modules
            if module in self.application_modules:
                missing_domains = self.domain_modules - deps
                if missing_domains:
                    warning(f"{module} does not depend on all domain modules: missing {missing_domains}")
                    self.has_warnings = True

        if not self.has_errors:
            success("All dependency rules passed")

    def detect_circular_dependencies(self):
        """Detect circular dependencies"""
        info("\nChecking for circular dependencies...")

        def has_cycle(start, visited, rec_stack):
            visited.add(start)
            rec_stack.add(start)

            for neighbor in self.dependencies.get(start, set()):
                if neighbor not in visited:
                    if has_cycle(neighbor, visited, rec_stack):
                        return True
                elif neighbor in rec_stack:
                    return True

            rec_stack.remove(start)
            return False

        visited = set()
        for module in self.modules.keys():
            if module not in visited:
                if has_cycle(module, visited, set()):
                    error(f"Circular dependency detected involving {module}")
                    self.has_errors = True
                    return

        success("No circular dependencies detected")

    def validate_package_structure(self):
        """Validate package structure conventions"""
        info("\nValidating package structure...")

        conventions = {
            'controller': 'REST controllers should be in controller package',
            'service': 'Business logic should be in service package',
            'mapper': 'MyBatis mappers should be in mapper package',
            'dto': 'DTOs should be in dto package',
            'entity': 'Domain entities should be in entity package',
        }

        for module_name, module_path in self.modules.items():
            if module_name in self.domain_modules:
                java_dir = module_path / 'src' / 'main' / 'java' / 'com' / 'inspecthub' / module_name

                if not java_dir.exists():
                    continue

                found_packages = set()
                for item in java_dir.iterdir():
                    if item.is_dir():
                        found_packages.add(item.name)

                # Check for expected packages
                expected_packages = {'controller', 'service', 'dto', 'mapper'}
                missing = expected_packages - found_packages

                if missing and module_name != 'common':
                    warning(f"{module_name} is missing recommended packages: {missing}")
                    self.has_warnings = True

        if not self.has_warnings:
            success("Package structure looks good")

    def validate_resources(self):
        """Validate resources directory"""
        info("\nValidating resources...")

        for module_name, module_path in self.modules.items():
            if module_name in self.domain_modules:
                mapper_dir = module_path / 'src' / 'main' / 'resources' / 'mybatis' / 'mapper'

                if mapper_dir.exists():
                    xml_files = list(mapper_dir.glob('*.xml'))
                    if xml_files:
                        success(f"{module_name} has {len(xml_files)} MyBatis mapper(s)")
                    else:
                        warning(f"{module_name} has mybatis/mapper directory but no XML files")
                        self.has_warnings = True

    def generate_dependency_graph(self):
        """Generate dependency graph in DOT format"""
        info("\nGenerating dependency graph...")

        dot = ["digraph modules {"]
        dot.append('  rankdir=LR;')
        dot.append('  node [shape=box];')
        dot.append('')

        # Define node styles
        dot.append('  // Infrastructure')
        for module in self.infrastructure_modules:
            if module in self.modules:
                dot.append(f'  {module} [style=filled,fillcolor=lightblue];')

        dot.append('')
        dot.append('  // Domain')
        for module in self.domain_modules:
            if module in self.modules:
                dot.append(f'  {module} [style=filled,fillcolor=lightgreen];')

        dot.append('')
        dot.append('  // Application')
        for module in self.application_modules:
            if module in self.modules:
                dot.append(f'  {module} [style=filled,fillcolor=lightyellow];')

        dot.append('')
        dot.append('  // Dependencies')
        for module, deps in sorted(self.dependencies.items()):
            for dep in sorted(deps):
                dot.append(f'  {module} -> {dep};')

        dot.append('}')

        graph_file = self.backend_dir / 'module-dependencies.dot'
        graph_file.write_text('\n'.join(dot), encoding='utf-8')

        success(f"Dependency graph saved to: {graph_file}")
        info("Generate PNG: dot -Tpng module-dependencies.dot -o module-dependencies.png")

    def run(self):
        """Run all validations"""
        print("🔍 Module Dependency Validator\n")
        print("=" * 60)

        self.discover_modules()
        self.parse_dependencies()
        self.validate_dependency_rules()
        self.detect_circular_dependencies()
        self.validate_package_structure()
        self.validate_resources()
        self.generate_dependency_graph()

        print("\n" + "=" * 60)

        if self.has_errors:
            error("\nValidation FAILED - please fix errors above")
            sys.exit(1)
        elif self.has_warnings:
            warning("\nValidation PASSED with warnings - please review")
            sys.exit(0)
        else:
            success("\nValidation PASSED - all modules are properly configured!")
            sys.exit(0)


def main():
    script_dir = Path(__file__).parent
    backend_dir = script_dir.parent

    validator = ModuleValidator(backend_dir)
    validator.run()


if __name__ == '__main__':
    main()
